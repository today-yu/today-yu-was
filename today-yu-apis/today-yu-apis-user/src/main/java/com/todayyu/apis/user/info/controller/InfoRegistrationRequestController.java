package com.todayyu.apis.user.info.controller;

import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.apis.user.common.dto.CursorResponse;
import com.todayyu.apis.user.info.dto.request.GetInfoRegistrationRequestsRequest;
import com.todayyu.apis.user.info.dto.request.SubmitInfoRegistrationRequestRequest;
import com.todayyu.apis.user.info.dto.response.InfoRegistrationRequestCursorResponse;
import com.todayyu.apis.user.info.dto.response.InfoRegistrationRequestDetailResponse;
import com.todayyu.apis.user.info.dto.response.InfoRegistrationRequestSummaryResponse;
import com.todayyu.apis.user.info.dto.response.SubmitInfoRegistrationRequestResponse;
import com.todayyu.application.common.dto.CursorResult;
import com.todayyu.application.info.dto.query.GetInfoRegistrationRequestDetailQuery;
import com.todayyu.application.info.dto.result.InfoRegistrationRequestDetailResult;
import com.todayyu.application.info.dto.result.InfoRegistrationRequestSummaryResult;
import com.todayyu.application.info.dto.result.SubmitInfoRegistrationRequestResult;
import com.todayyu.application.info.port.dto.InfoRegistrationRequestCursor;
import com.todayyu.application.info.usecase.GetInfoRegistrationRequestDetailUsecase;
import com.todayyu.application.info.usecase.GetInfoRegistrationRequestsUsecase;
import com.todayyu.application.info.usecase.SubmitInfoRegistrationRequestUsecase;
import com.todayyu.domain.info.vo.InfoRegistrationRequestId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/infos/registration-requests")
@RequiredArgsConstructor
public class InfoRegistrationRequestController {
    private final GetInfoRegistrationRequestsUsecase getInfoRegistrationRequestsUsecase;
    private final GetInfoRegistrationRequestDetailUsecase getInfoRegistrationRequestDetailUsecase;
    private final SubmitInfoRegistrationRequestUsecase submitInfoRegistrationRequestUsecase;
    
    @GetMapping
    public ResponseEntity<?> getInfoRegistrationRequests(
            @Valid GetInfoRegistrationRequestsRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CursorResult<InfoRegistrationRequestSummaryResult, InfoRegistrationRequestCursor> result
                = getInfoRegistrationRequestsUsecase.getInfoRegistrationRequests(request.toQuery(userDetails.getMemberId()));
        
        return ResponseEntity.ok(
                SuccessResponse.from(
                        "정보 등록 요청 조회에 성공하였습니다.",
                        CursorResponse.of(
                                result,
                                InfoRegistrationRequestSummaryResponse::from,
                                InfoRegistrationRequestCursorResponse::from
                        )
                )
        );
    }
    
    @GetMapping("/{requestId}")
    public ResponseEntity<?> getInfoRegistrationRequestDetail(
            @PathVariable(name = "requestId") Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        GetInfoRegistrationRequestDetailQuery query = new GetInfoRegistrationRequestDetailQuery(
                new InfoRegistrationRequestId(requestId),
                userDetails.getMemberId()
        );
        
        InfoRegistrationRequestDetailResult result
                = getInfoRegistrationRequestDetailUsecase.getInfoRegistrationRequestDetail(query);
        
        return ResponseEntity.ok(SuccessResponse.from(
                "정보 등록 요청 상세 조회에 성공하였습니다.",
                InfoRegistrationRequestDetailResponse.from(result)
        ));
    }
    
    @PostMapping
    public ResponseEntity<?> submit(
            @Valid @RequestBody SubmitInfoRegistrationRequestRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SubmitInfoRegistrationRequestResult result = submitInfoRegistrationRequestUsecase.submit(
                request.toCommand(userDetails.getMemberId())
        );
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.from(
                        "정보 등록 요청 제출에 성공하였습니다.",
                        SubmitInfoRegistrationRequestResponse.from(result)
                ));
    }
}
