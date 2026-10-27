package com.todayyu.apis.user.member.controller;

import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.user.member.dto.request.IssueEmailVerificationRequest;
import com.todayyu.apis.user.member.dto.request.SignupRequest;
import com.todayyu.apis.user.member.dto.request.VerifyEmailVerificationRequest;
import com.todayyu.application.member.usecase.IssueEmailVerificationUsecase;
import com.todayyu.application.member.usecase.SignupUsecase;
import com.todayyu.application.member.usecase.VerifyEmailVerificationUsecase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class SignupController {
    private final IssueEmailVerificationUsecase issueEmailVerificationUsecase;
    private final VerifyEmailVerificationUsecase verifyEmailVerificationUsecase;
    private final SignupUsecase signupUsecase;
    
    @PostMapping("/email-verification/issue")
    public ResponseEntity<?> issueEmailVerification(@Valid @RequestBody IssueEmailVerificationRequest request) {
        issueEmailVerificationUsecase.issue(request.toCommand());
        return ResponseEntity.ok(SuccessResponse.from("이메일 인증 코드 발급에 성공하였습니다."));
    }
    
    @PostMapping("/email-verification/verify")
    public ResponseEntity<?> verifyEmailVerification(@Valid @RequestBody VerifyEmailVerificationRequest request) {
        verifyEmailVerificationUsecase.verify(request.toCommand());
        return ResponseEntity.ok(SuccessResponse.from("이메일 인증에 성공하였습니다."));
    }
    
    @PostMapping("/sign-up")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        signupUsecase.signup(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.from("회원가입에 성공하였습니다."));
    }
}
