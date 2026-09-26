package com.todayyu.apis.common.exception;

import com.todayyu.common.error.BaseErrorType;
import com.todayyu.common.error.CommonErrorType;
import com.todayyu.common.error.ErrorCategory;
import com.todayyu.common.exception.CustomException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("CustomException은 에러 카테고리에 맞는 상태 코드와 에러 코드로 응답한다")
    void customException() throws Exception {
        mockMvc.perform(get("/test/custom"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("TEST-0001"))
                .andExpect(jsonPath("$.message").value("테스트 충돌 에러입니다."))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    @DisplayName("CustomException에 data가 있으면 응답 본문에 함께 담는다")
    void customExceptionWithData() throws Exception {
        mockMvc.perform(get("/test/custom-with-data"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("TEST-0001"))
                .andExpect(jsonPath("$.data.remainingSeconds").value(30));
    }

    @Test
    @DisplayName("처리되지 않은 예외는 500과 서버 에러 코드로 응답한다")
    void unknownException() throws Exception {
        mockMvc.perform(get("/test/unknown"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.SERVER_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.message").value(CommonErrorType.SERVER_ERROR.getMessage()));
    }

    @Test
    @DisplayName("요청 인자 유효성 검증 실패 시 422와 필드별 에러 메시지로 응답한다")
    void validationFailure() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "홍길동", "nickname": "a"}
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.VALIDATION_ERROR.getErrorCode()))
                .andExpect(jsonPath("$.errors.nickname").value("닉네임은 2자 이상이어야 합니다."))
                .andExpect(jsonPath("$.errors.name").doesNotExist());
    }

    @Test
    @DisplayName("한 필드에서 여러 제약이 실패해도 예외 없이 하나의 메시지로 응답한다")
    void validationFailureOnMultipleConstraints() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "홍길동", "nickname": ""}
                                """))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.nickname").value(anyOf(
                        equalTo("닉네임은 필수입니다."),
                        equalTo("닉네임은 2자 이상이어야 합니다.")
                )));
    }

    @Test
    @DisplayName("존재하지 않는 URL은 404와 NOT_FOUND 에러 코드로 응답한다")
    void noHandlerFound() throws Exception {
        mockMvc.perform(get("/test/not-exists"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.NOT_FOUND.getErrorCode()));
    }

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드는 405와 BAD_REQUEST 에러 코드로 응답한다")
    void methodNotAllowed() throws Exception {
        mockMvc.perform(post("/test/unknown"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.BAD_REQUEST.getErrorCode()));
    }

    @Test
    @DisplayName("지원하지 않는 Content-Type은 415와 BAD_REQUEST 에러 코드로 응답한다")
    void unsupportedMediaType() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("text"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.BAD_REQUEST.getErrorCode()));
    }

    @Test
    @DisplayName("읽을 수 없는 요청 본문은 400과 BAD_REQUEST 에러 코드로 응답한다")
    void messageNotReadable() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.BAD_REQUEST.getErrorCode()));
    }

    @Test
    @DisplayName("5xx로 처리되는 Spring MVC 예외는 서버 에러 코드로 응답한다")
    void springMvcServerError() throws Exception {
        mockMvc.perform(get("/test/missing-path-variable"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value(CommonErrorType.SERVER_ERROR.getErrorCode()));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/custom")
        void custom() {
            throw new CustomException(TestErrorType.CONFLICT);
        }

        @GetMapping("/test/custom-with-data")
        void customWithData() {
            throw new CustomException(TestErrorType.CONFLICT, null, Map.of("remainingSeconds", 30));
        }

        @GetMapping("/test/unknown")
        void unknown() {
            throw new IllegalStateException("unexpected");
        }

        @PostMapping("/test/validation")
        void validation(@Valid @RequestBody TestRequest request) {
        }

        // 매핑 경로에 없는 path variable을 요구하여 MissingPathVariableException(500) 발생
        @GetMapping("/test/missing-path-variable")
        void missingPathVariable(@PathVariable("id") String id) {
        }
    }

    record TestRequest(
            @NotBlank(message = "이름은 필수입니다.")
            String name,

            @NotBlank(message = "닉네임은 필수입니다.")
            @Size(min = 2, message = "닉네임은 2자 이상이어야 합니다.")
            String nickname
    ) {
    }

    enum TestErrorType implements BaseErrorType {
        CONFLICT(ErrorCategory.CONFLICT, "TEST-0001", "테스트 충돌 에러입니다."),
        ;

        private final ErrorCategory category;
        private final String errorCode;
        private final String message;

        TestErrorType(ErrorCategory category, String errorCode, String message) {
            this.category = category;
            this.errorCode = errorCode;
            this.message = message;
        }

        @Override
        public ErrorCategory getCategory() {
            return category;
        }

        @Override
        public String getErrorCode() {
            return errorCode;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}
