package com.todayyu.apis.user.auth.controller;

import com.todayyu.apis.common.auth.AuthScheme;
import com.todayyu.apis.common.response.SuccessResponse;
import com.todayyu.apis.user.auth.dto.ReissueAuthTokenRequest;
import com.todayyu.apis.user.auth.dto.SigninRequest;
import com.todayyu.apis.user.auth.dto.AuthTokenResponse;
import com.todayyu.application.auth.dto.result.AuthTokenResult;
import com.todayyu.application.auth.dto.command.SignoutCommand;
import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.application.auth.usecase.ReissueAuthTokenUsecase;
import com.todayyu.application.auth.usecase.SigninUsecase;
import com.todayyu.application.auth.usecase.SignoutUsecase;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.auth.vo.AccessToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final SigninUsecase signinUsecase;
    private final SignoutUsecase signoutUsecase;
    private final ReissueAuthTokenUsecase reissueAuthTokenUsecase;
    
    private static final String AUTHORIZATION_PREFIX = AuthScheme.BEARER + " ";
    
    @PostMapping("/sign-in")
    public ResponseEntity<?> signin(@Valid @RequestBody SigninRequest request) {
        AuthTokenResult result = signinUsecase.signin(request.toCommand());
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(
                        SuccessResponse.from(
                                "로그인에 성공하였습니다.",
                                AuthTokenResponse.from(result)
                        )
                );
    }
    
    @PostMapping("/sign-out")
    public ResponseEntity<?> signout(@RequestHeader(value = "authorization") String authorization) {
        AccessToken accessToken = parseAccessToken(authorization);
        signoutUsecase.signout(new SignoutCommand(accessToken));
        return ResponseEntity.ok(SuccessResponse.from("로그아웃에 성공하였습니다."));
    }
    
    @PostMapping("/reissue")
    public ResponseEntity<?> reissueAuthToken(@Valid @RequestBody ReissueAuthTokenRequest request) {
        AuthTokenResult result = reissueAuthTokenUsecase.reissue(request.toCommand());
        
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(
                        SuccessResponse.from(
                                "인증 토큰 재발급에 성공하였습니다.",
                                AuthTokenResponse.from(result)
                        )
                );
    }
    
    /**
     * {@code Authorization} 헤더의 값에서 Access Token을 추출하는 메서드
     *
     * @param authorization {@code Authorization} 헤더의 값
     * @return Access Token
     */
    private AccessToken parseAccessToken(String authorization) {
        String token = authorization.substring(AUTHORIZATION_PREFIX.length()).strip();
        
        if (token.isEmpty()) {
            throw new CustomException(BusinessAuthErrorType.INVALID_TOKEN, "Access Token이 비어있습니다.");
        }
        
        return new AccessToken(token);
    }
}
