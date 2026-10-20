package com.todayyu.apis.user.inquiry.controller;

import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.common.security.principal.CustomUserDetails;
import com.todayyu.apis.user.inquiry.dto.request.SubmitInquiryRequest;
import com.todayyu.apis.user.inquiry.dto.response.SubmitInquiryResponse;
import com.todayyu.application.inquiry.dto.result.SubmitInquiryResult;
import com.todayyu.application.inquiry.usecase.SubmitInquiryUsecase;
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
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryController {
    private final SubmitInquiryUsecase submitInquiryUsecase;
    
    @PostMapping
    public ResponseEntity<?> submit(
            @Valid @RequestBody SubmitInquiryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        SubmitInquiryResult result = submitInquiryUsecase.submit(request.toCommand(userDetails.getMemberId()));
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.from(
                        "문의 작성에 성공하였습니다.",
                        new SubmitInquiryResponse(result.id().getValue())
                ));
    }
}
