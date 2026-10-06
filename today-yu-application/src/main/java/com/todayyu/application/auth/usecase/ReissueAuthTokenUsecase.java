package com.todayyu.application.auth.usecase;

import com.todayyu.application.auth.dto.AuthTokenResult;
import com.todayyu.application.auth.dto.IssuedAccessToken;
import com.todayyu.application.auth.dto.IssuedRefreshToken;
import com.todayyu.application.auth.dto.RefreshTokenClaims;
import com.todayyu.application.auth.dto.ReissueAuthTokenCommand;
import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.application.auth.port.TokenPort;
import com.todayyu.application.auth.port.read.ReadRefreshTokenPort;
import com.todayyu.application.auth.port.save.SaveRefreshTokenPort;
import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.auth.vo.RefreshToken;
import com.todayyu.domain.auth.vo.SessionId;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.vo.MemberId;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Usecase
@RequiredArgsConstructor
public class ReissueAuthTokenUsecase {
    private final TokenPort tokenPort;
    private final ReadRefreshTokenPort readRefreshTokenPort;
    private final SaveRefreshTokenPort saveRefreshTokenPort;
    private final ReadMemberPort readMemberPort;
    
    /**
     * 인증 토큰 재발급 메서드
     *
     * <p> Refresh Token 파싱 중 {@link CustomException}이 발생하면, {@link BusinessAuthErrorType#FAIL_REISSUE} 에러 타입
     *     으로 변환하여 예외를 발생시킨다.</p>
     *
     * <p> Refresh Token에 존재하는 {@link SessionId} 클레임과 동일한 클레임으로 새로운 인증 토큰을 발급한다. </p>
     *
     * <p> 새로 발급한 Refresh Token은 덮어씌우는 형태로 저장한다. </p>
     *
     * @param command 인증 토큰 재발급 요청 DTO
     * @return 새로 발급한 인증 토큰 인증 토큰
     * @throws CustomException 인증 토큰 재발급 중 예외가 발생한 경우
     * <ul>
     *     <li> {@link BusinessAuthErrorType#FAIL_REISSUE} - Refresh Token 파싱에 실패한 경우 </li>
     *     <li> {@link BusinessAuthErrorType#FAIL_REISSUE} - Refresh Token이 존재하지 않는 경우 </li>
     *     <li> {@link BusinessAuthErrorType#FAIL_REISSUE} - Refresh Token이 일치하지 않는 경우 </li>
     *     <li> {@link BusinessAuthErrorType#FAIL_REISSUE} - 회원이 존재하지 않는 경우 </li>
     * </ul>
     */
    public AuthTokenResult reissue(ReissueAuthTokenCommand command) {
        RefreshToken refreshToken = command.refreshToken();
        RefreshTokenClaims claims = null;
        
        try {
            claims = tokenPort.parseRefreshToken(refreshToken);
        } catch (CustomException e) {
            throw new CustomException(BusinessAuthErrorType.FAIL_REISSUE, "Refresh Token 파싱에 실패하였습니다.", e);
        }
        
        MemberId memberId = claims.memberId();
        SessionId sessionId = claims.sessionId();
        
        RefreshToken savedRefreshToken = readRefreshTokenPort.read(memberId, sessionId)
                .orElseThrow(() -> new CustomException(BusinessAuthErrorType.FAIL_REISSUE, "Refresh Token이 존재하지 않습니다."));
        
        if (!refreshToken.equals(savedRefreshToken)) {
            throw new CustomException(BusinessAuthErrorType.FAIL_REISSUE, "Refresh Token이 일치하지 않습니다.");
        }
        
        Member member = readMemberPort.readByMemberId(memberId)
                .orElseThrow(() -> new CustomException(
                        BusinessAuthErrorType.FAIL_REISSUE,
                        "해당 회원 ID를 가진 회원이 존재하지 않습니다."
                ));
        
        Instant now = Instant.now();
        IssuedAccessToken issuedAccessToken = tokenPort.issueAccessToken(memberId, member.getRole(), sessionId, now);
        IssuedRefreshToken issuedRefreshToken = tokenPort.issueRefreshToken(memberId, sessionId, now);
        
        saveRefreshTokenPort.save(memberId, sessionId, issuedRefreshToken);
        
        return AuthTokenResult.from(issuedAccessToken, issuedRefreshToken);
    }
}
