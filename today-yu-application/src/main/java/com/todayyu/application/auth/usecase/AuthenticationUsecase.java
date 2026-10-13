package com.todayyu.application.auth.usecase;

import com.todayyu.application.auth.port.dto.AccessTokenClaims;
import com.todayyu.application.auth.dto.result.AuthenticatedMemberResult;
import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.application.auth.port.TokenPort;
import com.todayyu.application.auth.port.read.ReadBlacklistTokenPort;
import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.auth.vo.AccessToken;
import com.todayyu.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;

@Usecase
@RequiredArgsConstructor
public class AuthenticationUsecase {
    private final TokenPort tokenPort;
    private final ReadMemberPort readMemberPort;
    private final ReadBlacklistTokenPort readBlacklistTokenPort;
    
    /**
     * 인증을 위한 회원 정보({@link AuthenticatedMemberResult})를 조회하는 메서드
     *
     * <p> Access Token이 블랙리스트로 등록된 토큰이라면 예외를 발생한다. </p>
     * <p> Access Token 클레임의 회원 ID에 해당하는 회원이 존재하지 않을 경우 예외를 발생한다. </p>
     *
     * @param accessToken Access Token
     * @return 인증용 회원 정보
     * @throws CustomException {@link BusinessAuthErrorType#INVALID_TOKEN} - 블랙리스트로 등록된 토큰이거나, 회원이 존재하지 않는 경우
     */
    public AuthenticatedMemberResult authenticate(AccessToken accessToken) {
        AccessTokenClaims claims = tokenPort.parseAccessToken(accessToken);
        if (readBlacklistTokenPort.isBlacklistToken(claims.tokenId())) {
            throw new CustomException(BusinessAuthErrorType.INVALID_TOKEN, "블랙리스트로 등록된 토큰입니다.");
        }
        
        Member member = readMemberPort.readByMemberId(claims.memberId())
                .orElseThrow(() -> new CustomException(BusinessAuthErrorType.INVALID_TOKEN, "존재하지 않는 사용자입니다."));
        
        return new AuthenticatedMemberResult(member.getId(), member.getRole());
    }
}
