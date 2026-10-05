package com.todayyu.application.auth.usecase;

import com.todayyu.application.auth.dto.AccessTokenClaims;
import com.todayyu.application.auth.dto.AuthenticatedMember;
import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.application.auth.port.TokenPort;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.auth.vo.AccessToken;
import com.todayyu.domain.auth.vo.TokenId;
import com.todayyu.domain.member.entity.LocalAccount;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.member.vo.Email;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.domain.member.vo.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthenticationUsecaseTest {
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final AccessToken ACCESS_TOKEN = new AccessToken("access-token");
    private static final AccessTokenClaims CLAIMS =
            new AccessTokenClaims(MEMBER_ID, new TokenId("token-id"), Role.MEMBER, Instant.parse("2026-10-04T00:30:00Z"));

    @Mock
    private TokenPort tokenPort;

    @Mock
    private ReadMemberPort readMemberPort;

    @InjectMocks
    private AuthenticationUsecase authenticationUsecase;

    @Test
    @DisplayName("Access Token이 유효하고 회원이 존재하면 회원 아이디와 권한을 반환한다")
    void authenticate() {
        // given
        given(tokenPort.parseAccessToken(ACCESS_TOKEN)).willReturn(CLAIMS);
        given(readMemberPort.readByMemberId(MEMBER_ID)).willReturn(Optional.of(member()));

        // when
        AuthenticatedMember result = authenticationUsecase.extractMemberIdFromAccessToken(ACCESS_TOKEN);

        // then
        assertEquals(MEMBER_ID, result.memberId());
        assertEquals(Role.MEMBER, result.role());
    }

    @Test
    @DisplayName("토큰의 회원이 존재하지 않으면 유효하지 않은 토큰 예외가 발생한다")
    void authenticateWithNotExistMember() {
        // given
        given(tokenPort.parseAccessToken(ACCESS_TOKEN)).willReturn(CLAIMS);
        given(readMemberPort.readByMemberId(MEMBER_ID)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> authenticationUsecase.extractMemberIdFromAccessToken(ACCESS_TOKEN));

        // then
        assertEquals(BusinessAuthErrorType.INVALID_TOKEN, exception.getErrorType());
    }

    @Test
    @DisplayName("Access Token 파싱에 실패하면 예외를 그대로 전파하고 회원을 조회하지 않는다")
    void authenticateWithInvalidToken() {
        // given
        given(tokenPort.parseAccessToken(ACCESS_TOKEN))
                .willThrow(new CustomException(BusinessAuthErrorType.EXPIRED_TOKEN));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> authenticationUsecase.extractMemberIdFromAccessToken(ACCESS_TOKEN));

        // then
        assertEquals(BusinessAuthErrorType.EXPIRED_TOKEN, exception.getErrorType());
        then(readMemberPort).should(never()).readByMemberId(any());
    }

    private Member member() {
        return Member.withId(
                MEMBER_ID,
                new Email("user@example.com"),
                new Nickname("명랑한 영대인 1"),
                Role.MEMBER,
                List.of(LocalAccount.withId(new AccountId(1L), new EncodedPassword("encoded-password")))
        );
    }
}
