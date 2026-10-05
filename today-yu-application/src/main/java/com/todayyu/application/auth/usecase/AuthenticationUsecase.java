package com.todayyu.application.auth.usecase;

import com.todayyu.application.auth.dto.AuthenticatedMember;
import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.application.auth.port.TokenPort;
import com.todayyu.application.common.annotation.Usecase;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.auth.vo.AccessToken;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.vo.MemberId;
import lombok.RequiredArgsConstructor;

@Usecase
@RequiredArgsConstructor
public class AuthenticationUsecase {
    private final TokenPort tokenPort;
    private final ReadMemberPort readMemberPort;
    
    /**
     * 인증을 위한 회원 정보({@link AuthenticatedMember})를 조회하는 메서드
     *
     * @param accessToken Access Token
     * @return 인증용 회원 정보
     * @throws CustomException {@link BusinessAuthErrorType#INVALID_TOKEN} - 회원이 존재하지 않는 경우
     */
    public AuthenticatedMember extractMemberIdFromAccessToken(AccessToken accessToken) {
        MemberId memberId = tokenPort.parseAccessToken(accessToken).memberId();
        
        Member member = readMemberPort.readByMemberId(memberId)
                .orElseThrow(() -> new CustomException(BusinessAuthErrorType.INVALID_TOKEN, "존재하지 않는 사용자입니다."));
        
        return new AuthenticatedMember(member.getId(), member.getRole());
    }
}
