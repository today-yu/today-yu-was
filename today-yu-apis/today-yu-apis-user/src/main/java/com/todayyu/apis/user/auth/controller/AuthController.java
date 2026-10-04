package com.todayyu.apis.user.auth.controller;

import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.user.auth.dto.SigninRequest;
import com.todayyu.apis.user.auth.dto.SigninResponse;
import com.todayyu.application.auth.dto.SigninResult;
import com.todayyu.application.auth.usecase.SigninUsecase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final SigninUsecase signinUsecase;
    
    @PostMapping("/sign-in")
    public ResponseEntity<?> signin(@Valid @RequestBody SigninRequest request) {
        SigninResult result = signinUsecase.signin(request.toCommand());
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(
                        SuccessResponse.from(
                                "로그인에 성공하였습니다.",
                                SigninResponse.from(result)
                        )
                );
    }
}
