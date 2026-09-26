package com.todayyu.apis.common.exception;

import com.todayyu.apis.common.response.ErrorResponse;
import com.todayyu.common.error.BaseErrorType;
import com.todayyu.common.error.CommonErrorType;
import com.todayyu.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    
    /**
     * {@link CustomException} 예외 처리 메서드
     *
     * <p> 응답 본문이 null일 경우에도 {@link ErrorResponse}에 의해 직렬화되지 않기에 항상 data 필드를 응답 본문에 포함한다. </p>
     *
     * @param ex {@link CustomException} 객체
     * @return 응답 객체
     */
    @ExceptionHandler(exception = CustomException.class)
    protected ResponseEntity<?> handleCustomException(final CustomException ex) {
        log.warn("[Error/Business] {}", ex.getMessage());
        
        return ResponseEntity.status(ErrorHttpStatusResolver.resolve(ex.getErrorType().getCategory()))
                .body(ErrorResponse.from(ex.getErrorType(), ex.getData()));
    }
    
    /**
     * {@link Exception} 예외 처리 메서드
     *
     * <p> 별도의 예외 처리를 하지 않은 예외에 대한 예외 처리를 진행한다. </p>
     *
     * @param ex {@link Exception} 예외 객체
     * @return 응답 객체
     */
    @ExceptionHandler(exception = Exception.class)
    protected ResponseEntity<?> handleException(final Exception ex) {
        log.error("[Error] {}", ex.getMessage(), ex);
        BaseErrorType errorType = CommonErrorType.SERVER_ERROR;
        
        return ResponseEntity.status(ErrorHttpStatusResolver.resolve(errorType.getCategory()))
                .body(ErrorResponse.from(errorType));
    }
    
    /**
     * 요청 인자 유효성 검증 실패 예외 처리 메서드
     *
     * @param ex {@link MethodArgumentNotValidException} 객체
     * @param headers HTTP 헤더
     * @param status HTTP 상태 코드
     * @param request HTTP 요청
     * @return 응답 객체
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        BaseErrorType errorType = CommonErrorType.VALIDATION_ERROR;
        Map<String, String> errors = parseMethodArgumentNotValidExceptionErrors(ex);
        HttpStatus responseStatus = ErrorHttpStatusResolver.resolve(errorType.getCategory());
        ErrorResponse responseBody = ErrorResponse.withErrors(errorType, errors);
        
        log.warn("[Error/Validation] {}", errors);
        
        return handleExceptionInternal(ex, responseBody, headers, responseStatus, request);
    }
    
    /**
     * {@link MethodArgumentNotValidException}에서 예외가 발생한 필드와 메시지를 Map으로 추출하는 메서드
     *
     * @param ex {@link MethodArgumentNotValidException} 객체
     * @return {@code <예외 발생 필드, 메시지>} 쌍의 Map
     */
    private Map<String, String> parseMethodArgumentNotValidExceptionErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors()
                .stream().collect(Collectors.toMap(
                        error -> error.getField(),
                        error -> Objects.requireNonNullElse(error.getDefaultMessage(), "유효하지 않은 값입니다."),
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }
    
    /**
     * Spring MVC 발생 예외 처리 메서드
     *
     * <p> 별도의 예외 처리를 하지 않은 모든 Spring MVC 예외를 처리한다. </p>
     *
     * <p> {@link #handleMethodArgumentNotValid(MethodArgumentNotValidException, HttpHeaders, HttpStatusCode, WebRequest)} 메서드와 같이
     *     이미 예외 처리를 진행하며 응답 본문이 {@link ErrorResponse} 타입의 객체인 예외는 추가 예외 처리를 진행하지 않는다. </p>
     *
     * @param ex {@link Exception} 객체
     * @param body HTTP 응답 본문
     * @param headers HTTP 헤더
     * @param statusCode HTTP 상태 코드
     * @param request HTTP 요청
     * @return 응답 객체
     */
    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        // 응답 본문을 이미 커스텀한 예외는 그대로 예외 응답 진행
        if (body instanceof ErrorResponse) {
            return super.handleExceptionInternal(ex, body, headers, statusCode, request);
        }
        
        if (statusCode.is5xxServerError()) {
            log.error("[Error/Spring MVC] {} - {}", statusCode, ex.getMessage(), ex);
        } else {
            log.warn("[Error/Spring MVC] {} - {}", statusCode, ex.getMessage());
        }
        
        ErrorResponse responseBody = ErrorResponse.from(parseHttpStatusCodeToErrorType(statusCode));
        return super.handleExceptionInternal(ex, responseBody, headers, statusCode, request);
    }
    
    /**
     * HTTP 상태 코드에 맞는 에러 타입으로 변환해 반환하는 메서드
     *
     * @param statusCode HTTP 응답 코드
     * @return {@link BaseErrorType} 타입의 인스턴스
     */
    private BaseErrorType parseHttpStatusCodeToErrorType(HttpStatusCode statusCode) {
        if (statusCode.is5xxServerError()) {
            return CommonErrorType.SERVER_ERROR;
        }
        
        if (statusCode.value() == HttpStatus.NOT_FOUND.value()) {
            return CommonErrorType.NOT_FOUND;
        }
        
        return CommonErrorType.BAD_REQUEST;
    }
}
