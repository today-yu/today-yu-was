package com.todayyu.apis.user.member.controller;

import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.user.member.dto.request.IssuePasswordResetVerificationRequest;
import com.todayyu.application.member.usecase.IssuePasswordResetVerificationUsecase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordController {
    private final IssuePasswordResetVerificationUsecase issuePasswordResetVerificationUsecase;
    
    @PostMapping("/password-reset-verification/issue")
    public ResponseEntity<?> issuePasswordResetVerification(
            @Valid @RequestBody IssuePasswordResetVerificationRequest request
    ) {
        issuePasswordResetVerificationUsecase.issue(request.toCommand());
        
        return ResponseEntity.ok(SuccessResponse.from("비밀번호 초기화 인증 코드 이메일 발송 요청에 성공하였습니다."));
    }
}
