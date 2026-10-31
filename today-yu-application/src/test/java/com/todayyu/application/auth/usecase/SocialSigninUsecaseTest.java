package com.todayyu.application.auth.usecase;

import com.todayyu.application.auth.dto.command.SocialSigninCommand;
import com.todayyu.application.auth.dto.result.AuthTokenResult;
import com.todayyu.application.auth.port.SocialAuthPort;
import com.todayyu.application.auth.port.TokenPort;
import com.todayyu.application.auth.port.dto.IssuedAccessToken;
import com.todayyu.application.auth.port.dto.IssuedRefreshToken;
import com.todayyu.application.auth.port.save.SaveRefreshTokenPort;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.application.member.usecase.SocialSignupUsecase;
import com.todayyu.domain.auth.vo.AccessToken;
import com.todayyu.domain.auth.vo.RefreshToken;
import com.todayyu.domain.auth.vo.SessionId;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.entity.SocialAccount;
import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SocialSigninUsecaseTest {
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final String AUTHORIZATION_CODE = "authorization-code";
    private static final SocialSigninCommand COMMAND = new SocialSigninCommand(AuthProvider.KAKAO, AUTHORIZATION_CODE);
    private static final SocialIdentity SOCIAL_IDENTITY =
            new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId("kakao-user-id"));
    private static final Instant ACCESS_TOKEN_EXPIRES_AT = Instant.parse("2026-11-01T00:30:00Z");
    private static final Instant REFRESH_TOKEN_EXPIRES_AT = Instant.parse("2026-11-15T00:00:00Z");
    private static final IssuedAccessToken ISSUED_ACCESS_TOKEN =
            new IssuedAccessToken(new AccessToken("access-token"), ACCESS_TOKEN_EXPIRES_AT);
    private static final IssuedRefreshToken ISSUED_REFRESH_TOKEN =
            new IssuedRefreshToken(new RefreshToken("refresh-token"), REFRESH_TOKEN_EXPIRES_AT);

    @Mock
    private ReadMemberPort readMemberPort;

    @Mock
    private SocialAuthPort socialAuthPort;

    @Mock
    private SocialSignupUsecase socialSignupUsecase;

    @Mock
    private TokenPort tokenPort;

    @Mock
    private SaveRefreshTokenPort saveRefreshTokenPort;

    @InjectMocks
    private SocialSigninUsecase socialSigninUsecase;

    @Captor
    private ArgumentCaptor<Instant> accessTokenIssuedAtCaptor;

    @Captor
    private ArgumentCaptor<Instant> refreshTokenIssuedAtCaptor;

    @Captor
    private ArgumentCaptor<SessionId> accessTokenSessionIdCaptor;

    @Captor
    private ArgumentCaptor<SessionId> refreshTokenSessionIdCaptor;

    @Test
    @DisplayName("가입된 소셜 회원이면 회원가입 없이 Access Token과 Refresh Token을 만료 시각과 함께 발급한다")
    void signinWithRegisteredMember() {
        // given
        given(socialAuthPort.authenticate(AuthProvider.KAKAO, AUTHORIZATION_CODE)).willReturn(SOCIAL_IDENTITY);
        given(readMemberPort.readBySocialIdentity(SOCIAL_IDENTITY)).willReturn(Optional.of(socialMember()));
        givenIssuedTokens();

        // when
        AuthTokenResult result = socialSigninUsecase.signin(COMMAND);

        // then
        assertEquals(ISSUED_ACCESS_TOKEN.accessToken(), result.accessToken());
        assertEquals(ACCESS_TOKEN_EXPIRES_AT, result.accessTokenExpiresAt());
        assertEquals(ISSUED_REFRESH_TOKEN.refreshToken(), result.refreshToken());
        assertEquals(REFRESH_TOKEN_EXPIRES_AT, result.refreshTokenExpiresAt());
        then(socialSignupUsecase).should(never()).signup(any());
    }

    @Test
    @DisplayName("가입되지 않은 사용자면 인증 결과로 회원가입한 뒤, 저장된 회원으로 인증 토큰을 발급한다")
    void signinWithNotRegisteredMember() {
        // given
        given(socialAuthPort.authenticate(AuthProvider.KAKAO, AUTHORIZATION_CODE)).willReturn(SOCIAL_IDENTITY);
        given(readMemberPort.readBySocialIdentity(SOCIAL_IDENTITY)).willReturn(Optional.empty());
        given(socialSignupUsecase.signup(SOCIAL_IDENTITY)).willReturn(socialMember());
        givenIssuedTokens();

        // when
        AuthTokenResult result = socialSigninUsecase.signin(COMMAND);

        // then
        then(socialSignupUsecase).should().signup(SOCIAL_IDENTITY);
        then(tokenPort).should().issueAccessToken(eq(MEMBER_ID), eq(Role.MEMBER), any(SessionId.class), any(Instant.class));
        then(tokenPort).should().issueRefreshToken(eq(MEMBER_ID), any(SessionId.class), any(Instant.class));
        assertSame(ISSUED_ACCESS_TOKEN.accessToken(), result.accessToken());
        assertSame(ISSUED_REFRESH_TOKEN.refreshToken(), result.refreshToken());
    }

    @Test
    @DisplayName("Access Token과 Refresh Token은 같은 발급 시각으로 발급한다")
    void signinWithSameIssuedAt() {
        // given
        given(socialAuthPort.authenticate(AuthProvider.KAKAO, AUTHORIZATION_CODE)).willReturn(SOCIAL_IDENTITY);
        given(readMemberPort.readBySocialIdentity(SOCIAL_IDENTITY)).willReturn(Optional.of(socialMember()));
        givenIssuedTokens();

        // when
        socialSigninUsecase.signin(COMMAND);

        // then
        then(tokenPort).should().issueAccessToken(eq(MEMBER_ID), eq(Role.MEMBER), any(SessionId.class), accessTokenIssuedAtCaptor.capture());
        then(tokenPort).should().issueRefreshToken(eq(MEMBER_ID), any(SessionId.class), refreshTokenIssuedAtCaptor.capture());
        assertEquals(accessTokenIssuedAtCaptor.getValue(), refreshTokenIssuedAtCaptor.getValue());
    }

    @Test
    @DisplayName("Access Token과 Refresh Token은 같은 세션 ID로 발급한다")
    void signinWithSameSessionId() {
        // given
        given(socialAuthPort.authenticate(AuthProvider.KAKAO, AUTHORIZATION_CODE)).willReturn(SOCIAL_IDENTITY);
        given(readMemberPort.readBySocialIdentity(SOCIAL_IDENTITY)).willReturn(Optional.of(socialMember()));
        givenIssuedTokens();

        // when
        socialSigninUsecase.signin(COMMAND);

        // then
        then(tokenPort).should().issueAccessToken(eq(MEMBER_ID), eq(Role.MEMBER), accessTokenSessionIdCaptor.capture(), any(Instant.class));
        then(tokenPort).should().issueRefreshToken(eq(MEMBER_ID), refreshTokenSessionIdCaptor.capture(), any(Instant.class));
        assertEquals(accessTokenSessionIdCaptor.getValue(), refreshTokenSessionIdCaptor.getValue());
    }

    @Test
    @DisplayName("로그인에 성공하면 발급한 Refresh Token을 회원 ID, 토큰 발급에 사용한 세션 ID와 함께 저장한다")
    void signinSavesRefreshToken() {
        // given
        given(socialAuthPort.authenticate(AuthProvider.KAKAO, AUTHORIZATION_CODE)).willReturn(SOCIAL_IDENTITY);
        given(readMemberPort.readBySocialIdentity(SOCIAL_IDENTITY)).willReturn(Optional.of(socialMember()));
        givenIssuedTokens();

        // when
        socialSigninUsecase.signin(COMMAND);

        // then
        then(tokenPort).should().issueRefreshToken(eq(MEMBER_ID), refreshTokenSessionIdCaptor.capture(), any(Instant.class));
        then(saveRefreshTokenPort).should().save(MEMBER_ID, refreshTokenSessionIdCaptor.getValue(), ISSUED_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("인증 제공자 인증에 실패하면 예외를 그대로 전파하고 회원 조회, 회원가입, 토큰 발급·저장을 하지 않는다")
    void signinWithFailedAuthentication() {
        // given
        RuntimeException authenticationException = new RuntimeException("인증 제공자 인증 실패");
        given(socialAuthPort.authenticate(AuthProvider.KAKAO, AUTHORIZATION_CODE)).willThrow(authenticationException);

        // when
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> socialSigninUsecase.signin(COMMAND));

        // then
        assertSame(authenticationException, exception);
        then(readMemberPort).shouldHaveNoInteractions();
        then(socialSignupUsecase).shouldHaveNoInteractions();
        then(tokenPort).shouldHaveNoInteractions();
        then(saveRefreshTokenPort).shouldHaveNoInteractions();
    }

    private void givenIssuedTokens() {
        given(tokenPort.issueAccessToken(eq(MEMBER_ID), eq(Role.MEMBER), any(SessionId.class), any(Instant.class)))
                .willReturn(ISSUED_ACCESS_TOKEN);
        given(tokenPort.issueRefreshToken(eq(MEMBER_ID), any(SessionId.class), any(Instant.class)))
                .willReturn(ISSUED_REFRESH_TOKEN);
    }

    private Member socialMember() {
        SocialAccount account = SocialAccount.withId(new AccountId(1L), SOCIAL_IDENTITY);
        return Member.withId(MEMBER_ID, null, new Nickname("명랑한 영대인 1"), Role.MEMBER, List.of(account));
    }
}
