package com.todayyu.application.auth.usecase;

import com.todayyu.application.auth.port.dto.IssuedAccessToken;
import com.todayyu.application.auth.port.dto.IssuedRefreshToken;
import com.todayyu.application.auth.dto.command.SigninCommand;
import com.todayyu.application.auth.dto.result.AuthTokenResult;
import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.application.auth.port.TokenPort;
import com.todayyu.application.auth.port.save.SaveRefreshTokenPort;
import com.todayyu.application.member.port.PasswordPort;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.auth.vo.AccessToken;
import com.todayyu.domain.auth.vo.RefreshToken;
import com.todayyu.domain.auth.vo.SessionId;
import com.todayyu.domain.member.entity.Account;
import com.todayyu.domain.member.entity.LocalAccount;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.entity.SocialAccount;
import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.domain.member.vo.Password;
import com.todayyu.domain.member.vo.Role;
import com.todayyu.domain.member.vo.SocialIdentity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SigninUsecaseTest {
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final Email EMAIL = new Email("user@example.com");
    private static final Password PASSWORD = new Password("password1234");
    private static final EncodedPassword ENCODED_PASSWORD = new EncodedPassword("encoded-password");
    private static final SigninCommand COMMAND = new SigninCommand(EMAIL, PASSWORD);
    private static final Instant ACCESS_TOKEN_EXPIRES_AT = Instant.parse("2026-10-04T00:30:00Z");
    private static final Instant REFRESH_TOKEN_EXPIRES_AT = Instant.parse("2026-10-18T00:00:00Z");
    private static final IssuedAccessToken ISSUED_ACCESS_TOKEN =
            new IssuedAccessToken(new AccessToken("access-token"), ACCESS_TOKEN_EXPIRES_AT);
    private static final IssuedRefreshToken ISSUED_REFRESH_TOKEN =
            new IssuedRefreshToken(new RefreshToken("refresh-token"), REFRESH_TOKEN_EXPIRES_AT);

    @Mock
    private ReadMemberPort readMemberPort;

    @Mock
    private PasswordPort passwordPort;

    @Mock
    private TokenPort tokenPort;

    @Mock
    private SaveRefreshTokenPort saveRefreshTokenPort;

    @InjectMocks
    private SigninUsecase signinUsecase;

    @Captor
    private ArgumentCaptor<Instant> accessTokenIssuedAtCaptor;

    @Captor
    private ArgumentCaptor<Instant> refreshTokenIssuedAtCaptor;

    @Captor
    private ArgumentCaptor<SessionId> accessTokenSessionIdCaptor;

    @Captor
    private ArgumentCaptor<SessionId> refreshTokenSessionIdCaptor;

    @Test
    @DisplayName("이메일과 비밀번호가 일치하면 Access Token과 Refresh Token을 만료 시각과 함께 발급한다")
    void signin() {
        // given
        given(readMemberPort.readByEmail(EMAIL)).willReturn(Optional.of(localMember()));
        given(passwordPort.matches(PASSWORD, ENCODED_PASSWORD)).willReturn(true);
        given(tokenPort.issueAccessToken(eq(MEMBER_ID), eq(Role.MEMBER), any(SessionId.class), any(Instant.class)))
                .willReturn(ISSUED_ACCESS_TOKEN);
        given(tokenPort.issueRefreshToken(eq(MEMBER_ID), any(SessionId.class), any(Instant.class)))
                .willReturn(ISSUED_REFRESH_TOKEN);

        // when
        AuthTokenResult result = signinUsecase.signin(COMMAND);

        // then
        assertEquals(ISSUED_ACCESS_TOKEN.accessToken(), result.accessToken());
        assertEquals(ACCESS_TOKEN_EXPIRES_AT, result.accessTokenExpiresAt());
        assertEquals(ISSUED_REFRESH_TOKEN.refreshToken(), result.refreshToken());
        assertEquals(REFRESH_TOKEN_EXPIRES_AT, result.refreshTokenExpiresAt());
    }

    @Test
    @DisplayName("Access Token과 Refresh Token은 같은 발급 시각으로 발급한다")
    void signinWithSameIssuedAt() {
        // given
        given(readMemberPort.readByEmail(EMAIL)).willReturn(Optional.of(localMember()));
        given(passwordPort.matches(PASSWORD, ENCODED_PASSWORD)).willReturn(true);
        given(tokenPort.issueAccessToken(eq(MEMBER_ID), eq(Role.MEMBER), any(SessionId.class), any(Instant.class)))
                .willReturn(ISSUED_ACCESS_TOKEN);
        given(tokenPort.issueRefreshToken(eq(MEMBER_ID), any(SessionId.class), any(Instant.class)))
                .willReturn(ISSUED_REFRESH_TOKEN);

        // when
        signinUsecase.signin(COMMAND);

        // then
        then(tokenPort).should().issueAccessToken(eq(MEMBER_ID), eq(Role.MEMBER), any(SessionId.class), accessTokenIssuedAtCaptor.capture());
        then(tokenPort).should().issueRefreshToken(eq(MEMBER_ID), any(SessionId.class), refreshTokenIssuedAtCaptor.capture());
        assertEquals(accessTokenIssuedAtCaptor.getValue(), refreshTokenIssuedAtCaptor.getValue());
    }

    @Test
    @DisplayName("Access Token과 Refresh Token은 같은 세션 ID로 발급한다")
    void signinWithSameSessionId() {
        // given
        given(readMemberPort.readByEmail(EMAIL)).willReturn(Optional.of(localMember()));
        given(passwordPort.matches(PASSWORD, ENCODED_PASSWORD)).willReturn(true);
        given(tokenPort.issueAccessToken(eq(MEMBER_ID), eq(Role.MEMBER), any(SessionId.class), any(Instant.class)))
                .willReturn(ISSUED_ACCESS_TOKEN);
        given(tokenPort.issueRefreshToken(eq(MEMBER_ID), any(SessionId.class), any(Instant.class)))
                .willReturn(ISSUED_REFRESH_TOKEN);

        // when
        signinUsecase.signin(COMMAND);

        // then
        then(tokenPort).should().issueAccessToken(eq(MEMBER_ID), eq(Role.MEMBER), accessTokenSessionIdCaptor.capture(), any(Instant.class));
        then(tokenPort).should().issueRefreshToken(eq(MEMBER_ID), refreshTokenSessionIdCaptor.capture(), any(Instant.class));
        assertEquals(accessTokenSessionIdCaptor.getValue(), refreshTokenSessionIdCaptor.getValue());
    }

    @Test
    @DisplayName("로그인에 성공하면 발급한 Refresh Token을 회원 ID, 토큰 발급에 사용한 세션 ID와 함께 저장한다")
    void signinSavesRefreshToken() {
        // given
        given(readMemberPort.readByEmail(EMAIL)).willReturn(Optional.of(localMember()));
        given(passwordPort.matches(PASSWORD, ENCODED_PASSWORD)).willReturn(true);
        given(tokenPort.issueAccessToken(eq(MEMBER_ID), eq(Role.MEMBER), any(SessionId.class), any(Instant.class)))
                .willReturn(ISSUED_ACCESS_TOKEN);
        given(tokenPort.issueRefreshToken(eq(MEMBER_ID), any(SessionId.class), any(Instant.class)))
                .willReturn(ISSUED_REFRESH_TOKEN);

        // when
        signinUsecase.signin(COMMAND);

        // then
        then(tokenPort).should().issueRefreshToken(eq(MEMBER_ID), refreshTokenSessionIdCaptor.capture(), any(Instant.class));
        then(saveRefreshTokenPort).should().save(MEMBER_ID, refreshTokenSessionIdCaptor.getValue(), ISSUED_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("가입되지 않은 이메일이면 더미 비밀번호와 비교한 뒤 로그인에 실패하고 토큰을 발급·저장하지 않는다")
    void signinWithNotRegisteredEmail() {
        // given
        given(readMemberPort.readByEmail(EMAIL)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> signinUsecase.signin(COMMAND));

        // then
        assertEquals(BusinessAuthErrorType.FAIL_SIGNIN, exception.getErrorType());
        then(passwordPort).should().matchesDummy(PASSWORD);
        then(passwordPort).should(never()).matches(any(), any());
        then(tokenPort).shouldHaveNoInteractions();
        then(saveRefreshTokenPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("로컬 계정이 없는 회원이면 더미 비밀번호와 비교한 뒤 로그인에 실패하고 토큰을 발급·저장하지 않는다")
    void signinWithoutLocalAccount() {
        // given
        given(readMemberPort.readByEmail(EMAIL)).willReturn(Optional.of(socialOnlyMember()));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> signinUsecase.signin(COMMAND));

        // then
        assertEquals(BusinessAuthErrorType.FAIL_SIGNIN, exception.getErrorType());
        then(passwordPort).should().matchesDummy(PASSWORD);
        then(passwordPort).should(never()).matches(any(), any());
        then(tokenPort).shouldHaveNoInteractions();
        then(saveRefreshTokenPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패하고 토큰을 발급·저장하지 않는다")
    void signinWithWrongPassword() {
        // given
        given(readMemberPort.readByEmail(EMAIL)).willReturn(Optional.of(localMember()));
        given(passwordPort.matches(PASSWORD, ENCODED_PASSWORD)).willReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> signinUsecase.signin(COMMAND));

        // then
        assertEquals(BusinessAuthErrorType.FAIL_SIGNIN, exception.getErrorType());
        then(passwordPort).should(never()).matchesDummy(any());
        then(tokenPort).shouldHaveNoInteractions();
        then(saveRefreshTokenPort).shouldHaveNoInteractions();
    }

    private Member localMember() {
        return member(LocalAccount.withId(new AccountId(1L), ENCODED_PASSWORD));
    }

    private Member socialOnlyMember() {
        SocialIdentity identity = new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId("kakao-user-id"));
        return member(SocialAccount.withId(new AccountId(1L), identity));
    }

    private Member member(Account account) {
        return Member.withId(MEMBER_ID, EMAIL, new Nickname("명랑한 영대인 1"), Role.MEMBER, List.of(account));
    }
}
