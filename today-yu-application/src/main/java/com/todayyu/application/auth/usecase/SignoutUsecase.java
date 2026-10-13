package com.todayyu.application.auth.usecase;

import com.todayyu.application.auth.dto.AccessTokenClaims;
import com.todayyu.application.auth.dto.command.SignoutCommand;
import com.todayyu.application.auth.port.TokenPort;
import com.todayyu.application.auth.port.save.SaveBlacklistTokenPort;
import com.todayyu.application.auth.port.save.SaveRefreshTokenPort;
import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.domain.auth.vo.SessionId;
import com.todayyu.domain.auth.vo.TokenId;
import com.todayyu.domain.member.vo.MemberId;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Usecase
@RequiredArgsConstructor
public class SignoutUsecase {
    private final TokenPort tokenPort;
    private final SaveBlacklistTokenPort saveBlacklistTokenPort;
    private final SaveRefreshTokenPort saveRefreshTokenPort;
    
    /**
     * 로그아웃 메서드
     *
     * <p> 로그아웃 후, 해당 멤버의 Access Token ID(jti)를 블랙리스트 토큰으로 저장한다. </p>
     *
     * <p> 로그아웃 후, 저장된 해당 세션의 Refresh Token을 제거한다. </p>
     *
     * @param command 로그아웃 요청 DTO
     */
    public void signout(SignoutCommand command) {
        AccessTokenClaims claims = tokenPort.parseAccessToken(command.accessToken());
        MemberId memberId = claims.memberId();
        SessionId sessionId = claims.sessionId();
        TokenId tokenId = claims.tokenId();
        Instant expiresAt = claims.expiresAt();
        
        saveBlacklistTokenPort.save(tokenId, expiresAt);
        
        saveRefreshTokenPort.delete(memberId, sessionId);
    }
}
