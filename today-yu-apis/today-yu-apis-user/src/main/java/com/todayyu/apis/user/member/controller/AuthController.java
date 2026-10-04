package com.todayyu.apis.user.member.controller;

import com.todayyu.apis.common.auth.AuthScheme;
import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.user.member.dto.SigninRequest;
import com.todayyu.apis.user.member.dto.SigninResponse;
import com.todayyu.apis.user.member.dto.SignupRequest;
import com.todayyu.apis.user.member.dto.VerificationCodeIssueRequest;
import com.todayyu.apis.user.member.dto.VerificationCodeVerifyRequest;
import com.todayyu.application.auth.dto.SigninResult;
import com.todayyu.application.auth.usecase.SigninUsecase;
import com.todayyu.application.member.usecase.EmailVerificationUsecase;
import com.todayyu.application.member.usecase.SignupUsecase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
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
    private final SignupUsecase signupUsecase;
    private final SigninUsecase signinUsecase;
    
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
    
    @PostMapping("/sign-up")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        signupUsecase.signup(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.from("회원가입에 성공하였습니다."));
    }
    
    @PostMapping("/sign-in")
    public ResponseEntity<?> signin(@Valid @RequestBody SigninRequest request) {
        SigninResult result = signinUsecase.signin(request.toCommand());
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noCache())
                .body(
                        SuccessResponse.from(
                                "로그인에 성공하였습니다.",
                                SigninResponse.from(result)
                        )
                );
    }
}
