package com.todayyu.apis.user.info.controller;

import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.apis.user.info.dto.SubmitInfoUpdateRequestRequest;
import com.todayyu.apis.user.info.dto.SubmitInfoUpdateRequestResponse;
import com.todayyu.application.info.dto.result.SubmitInfoUpdateRequestResult;
import com.todayyu.application.info.usecase.SubmitInfoUpdateRequestUsecase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/infos")
@RequiredArgsConstructor
public class InfoUpdateRequestController {
    private final SubmitInfoUpdateRequestUsecase submitInfoUpdateRequestUsecase;
    
    @PostMapping("/{infoId}/update-requests")
    public ResponseEntity<?> submit(
            @PathVariable(name = "infoId") Long infoId,
            @Valid @RequestBody SubmitInfoUpdateRequestRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SubmitInfoUpdateRequestResult result = submitInfoUpdateRequestUsecase.submit(
                request.toCommand(infoId, userDetails.getMemberId())
        );
    
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.from(
                        "정보 수정 요청 제출에 성공하였습니다.",
                        SubmitInfoUpdateRequestResponse.from(result)
                ));
    }
}
