package com.todayyu.apis.user.info.controller;

import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.apis.user.info.dto.SubmitInfoRegistrationRequestRequest;
import com.todayyu.apis.user.info.dto.SubmitInfoRegistrationRequestResponse;
import com.todayyu.application.info.dto.SubmitInfoRegistrationRequestResult;
import com.todayyu.application.info.usecase.SubmitInfoRegistrationRequestUsecase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/infos/registration-requests")
@RequiredArgsConstructor
public class InfoRegistrationRequestController {
    private final SubmitInfoRegistrationRequestUsecase submitInfoRegistrationRequestUsecase;
    
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
