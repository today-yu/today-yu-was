package com.todayyu.application.auth.usecase;

import com.todayyu.application.auth.dto.command.SocialSigninCommand;
import com.todayyu.application.auth.dto.result.AuthTokenResult;
import com.todayyu.application.auth.port.SocialAuthPort;
import com.todayyu.application.auth.port.TokenPort;
import com.todayyu.application.auth.port.dto.IssuedAccessToken;
import com.todayyu.application.auth.port.dto.IssuedRefreshToken;
import com.todayyu.application.auth.port.save.SaveRefreshTokenPort;
import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.application.member.usecase.SocialSignupUsecase;
import com.todayyu.domain.auth.vo.SessionId;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.SocialIdentity;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Usecase
@RequiredArgsConstructor
public class SocialSigninUsecase {
    private final ReadMemberPort readMemberPort;
    private final SocialAuthPort socialAuthPort;
    private final SocialSignupUsecase socialSignupUsecase;
    private final TokenPort tokenPort;
    private final SaveRefreshTokenPort saveRefreshTokenPort;

    /**
     * 소셜 로그인 메서드
     *
     * <p> 인증 제공자({@link AuthProvider})의 인가 코드로 사용자를 인증하고,
     *     인증 결과인 {@link SocialIdentity}로 회원을 조회한다. </p>
     *
     * <p> 가입되지 않은 사용자인 경우, {@link SocialSignupUsecase}를 통해 회원가입한 뒤 인증 토큰을 발급한다. </p>
     *
     * <p> 인증 제공자 호출 동안 DB 커넥션을 점유하지 않도록 해당 메서드에는 트랜잭션을 적용하지 않으며,
     *     회원가입은 {@link SocialSignupUsecase#signup(SocialIdentity)}의 트랜잭션에서 처리한다. </p>
     *
     * <p> 로그인에 성공하면 인증 토큰을 발급하며, 그 중 Refresh Token을 저장한다. </p>
     *
     * @param command 소셜 로그인 요청 DTO
     * @return 로그인 결과 {@link AuthTokenResult} 객체
     */
    public AuthTokenResult signin(SocialSigninCommand command) {
        AuthProvider provider = command.provider();
        String authorizationCode = command.authorizationCode();
        
        SocialIdentity identity = socialAuthPort.authenticate(provider, authorizationCode);
        
        Member member = readMemberPort.readBySocialIdentity(identity)
                .orElseGet(() -> socialSignupUsecase.signup(identity));
        
        SessionId sessionId = SessionId.create();
        Instant now = Instant.now();
        IssuedAccessToken accessToken = tokenPort.issueAccessToken(member.getId(), member.getRole(), sessionId, now);
        IssuedRefreshToken refreshToken = tokenPort.issueRefreshToken(member.getId(), sessionId, now);
        
        saveRefreshTokenPort.save(member.getId(), sessionId, refreshToken);
        
        return AuthTokenResult.from(accessToken, refreshToken);
    }
}
