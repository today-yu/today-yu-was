package com.todayyu.apis.user.member.controller;

import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.user.member.dto.VerificationCodeIssueRequest;
import com.todayyu.apis.user.member.dto.VerificationCodeVerifyRequest;
import com.todayyu.application.member.usecase.EmailVerificationUsecase;
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
public class AuthController {
    private final EmailVerificationUsecase emailVerificationUsecase;
    
    @PostMapping("/email-verification/issue")
    public ResponseEntity<?> issueVerificationCode(@Valid @RequestBody VerificationCodeIssueRequest request) {
        emailVerificationUsecase.issueVerificationCode(request.toCommand());
        return ResponseEntity.ok(SuccessResponse.from("이메일 인증 코드 발급에 성공하였습니다."));
    }
    
    @PostMapping("/email-verification/verify")
    public ResponseEntity<?> verifyVerificationCode(@Valid @RequestBody VerificationCodeVerifyRequest request) {
        emailVerificationUsecase.verify(request.toCommand());
        return ResponseEntity.ok(SuccessResponse.from("이메일 인증에 성공하였습니다."));
    }
}
