package com.todayyu.application.auth.usecase;

import com.todayyu.application.auth.dto.AuthTokenResult;
import com.todayyu.application.auth.dto.IssuedAccessToken;
import com.todayyu.application.auth.dto.IssuedRefreshToken;
import com.todayyu.application.auth.dto.RefreshTokenClaims;
import com.todayyu.application.auth.dto.command.ReissueAuthTokenCommand;
import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.application.auth.port.TokenPort;
import com.todayyu.application.auth.port.read.ReadRefreshTokenPort;
import com.todayyu.application.auth.port.save.SaveRefreshTokenPort;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.auth.vo.AccessToken;
import com.todayyu.domain.auth.vo.RefreshToken;
import com.todayyu.domain.auth.vo.SessionId;
import com.todayyu.domain.member.entity.LocalAccount;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.domain.member.vo.Role;
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

@ExtendWith(MockitoExtension.class)
class ReissueAuthTokenUsecaseTest {
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final SessionId SESSION_ID = SessionId.create();
    private static final RefreshToken REFRESH_TOKEN = new RefreshToken("refresh-token");
    private static final ReissueAuthTokenCommand COMMAND = new ReissueAuthTokenCommand(REFRESH_TOKEN);
    private static final RefreshTokenClaims CLAIMS =
            new RefreshTokenClaims(MEMBER_ID, SESSION_ID, Instant.parse("2026-10-18T00:00:00Z"));
    private static final Instant ACCESS_TOKEN_EXPIRES_AT = Instant.parse("2026-10-05T00:30:00Z");
    private static final Instant REFRESH_TOKEN_EXPIRES_AT = Instant.parse("2026-10-19T00:00:00Z");
    private static final IssuedAccessToken ISSUED_ACCESS_TOKEN =
            new IssuedAccessToken(new AccessToken("new-access-token"), ACCESS_TOKEN_EXPIRES_AT);
    private static final IssuedRefreshToken ISSUED_REFRESH_TOKEN =
            new IssuedRefreshToken(new RefreshToken("new-refresh-token"), REFRESH_TOKEN_EXPIRES_AT);

    @Mock
    private TokenPort tokenPort;

    @Mock
    private ReadRefreshTokenPort readRefreshTokenPort;

    @Mock
    private SaveRefreshTokenPort saveRefreshTokenPort;

    @Mock
    private ReadMemberPort readMemberPort;

    @InjectMocks
    private ReissueAuthTokenUsecase reissueAuthTokenUsecase;

    @Captor
    private ArgumentCaptor<Instant> accessTokenIssuedAtCaptor;

    @Captor
    private ArgumentCaptor<Instant> refreshTokenIssuedAtCaptor;

    @Test
    @DisplayName("저장된 Refresh Token과 일치하면 Access Token과 Refresh Token을 만료 시각과 함께 재발급한다")
    void reissue() {
        // given
        givenReissuable();

        // when
        AuthTokenResult result = reissueAuthTokenUsecase.reissue(COMMAND);

        // then
        assertEquals(ISSUED_ACCESS_TOKEN.accessToken(), result.accessToken());
        assertEquals(ACCESS_TOKEN_EXPIRES_AT, result.accessTokenExpiresAt());
        assertEquals(ISSUED_REFRESH_TOKEN.refreshToken(), result.refreshToken());
        assertEquals(REFRESH_TOKEN_EXPIRES_AT, result.refreshTokenExpiresAt());
    }

    @Test
    @DisplayName("기존 세션 ID와 현재 회원 Role로, 같은 발급 시각에 Access Token과 Refresh Token을 재발급한다")
    void reissueWithSameSessionIdAndCurrentRole() {
        // given
        givenReissuable();

        // when
        reissueAuthTokenUsecase.reissue(COMMAND);

        // then
        then(tokenPort).should().issueAccessToken(eq(MEMBER_ID), eq(Role.MEMBER), eq(SESSION_ID), accessTokenIssuedAtCaptor.capture());
        then(tokenPort).should().issueRefreshToken(eq(MEMBER_ID), eq(SESSION_ID), refreshTokenIssuedAtCaptor.capture());
        assertEquals(accessTokenIssuedAtCaptor.getValue(), refreshTokenIssuedAtCaptor.getValue());
    }

    @Test
    @DisplayName("재발급에 성공하면 새 Refresh Token을 기존 회원 ID, 세션 ID로 저장한다")
    void reissueSavesRefreshToken() {
        // given
        givenReissuable();

        // when
        reissueAuthTokenUsecase.reissue(COMMAND);

        // then
        then(saveRefreshTokenPort).should().save(MEMBER_ID, SESSION_ID, ISSUED_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("만료된 Refresh Token이면 재발급에 실패하고 토큰을 발급·저장하지 않는다")
    void reissueWithExpiredRefreshToken() {
        // given
        given(tokenPort.parseRefreshToken(REFRESH_TOKEN))
                .willThrow(new CustomException(BusinessAuthErrorType.EXPIRED_TOKEN));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> reissueAuthTokenUsecase.reissue(COMMAND));

        // then
        assertEquals(BusinessAuthErrorType.FAIL_REISSUE, exception.getErrorType());
        thenNotReissued();
        then(readRefreshTokenPort).shouldHaveNoInteractions();
        then(readMemberPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token이면 재발급에 실패하고 토큰을 발급·저장하지 않는다")
    void reissueWithInvalidRefreshToken() {
        // given
        given(tokenPort.parseRefreshToken(REFRESH_TOKEN))
                .willThrow(new CustomException(BusinessAuthErrorType.INVALID_TOKEN));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> reissueAuthTokenUsecase.reissue(COMMAND));

        // then
        assertEquals(BusinessAuthErrorType.FAIL_REISSUE, exception.getErrorType());
        thenNotReissued();
        then(readRefreshTokenPort).shouldHaveNoInteractions();
        then(readMemberPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("저장된 Refresh Token이 없으면 재발급에 실패하고 토큰을 발급·저장하지 않는다")
    void reissueWithoutSavedRefreshToken() {
        // given
        given(tokenPort.parseRefreshToken(REFRESH_TOKEN)).willReturn(CLAIMS);
        given(readRefreshTokenPort.read(MEMBER_ID, SESSION_ID)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> reissueAuthTokenUsecase.reissue(COMMAND));

        // then
        assertEquals(BusinessAuthErrorType.FAIL_REISSUE, exception.getErrorType());
        thenNotReissued();
        then(readMemberPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("저장된 Refresh Token과 일치하지 않으면 재발급에 실패하고 토큰을 발급·저장하지 않는다")
    void reissueWithMismatchedRefreshToken() {
        // given
        given(tokenPort.parseRefreshToken(REFRESH_TOKEN)).willReturn(CLAIMS);
        given(readRefreshTokenPort.read(MEMBER_ID, SESSION_ID))
                .willReturn(Optional.of(new RefreshToken("rotated-refresh-token")));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> reissueAuthTokenUsecase.reissue(COMMAND));

        // then
        assertEquals(BusinessAuthErrorType.FAIL_REISSUE, exception.getErrorType());
        thenNotReissued();
        then(readMemberPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("회원이 존재하지 않으면 재발급에 실패하고 토큰을 발급·저장하지 않는다")
    void reissueWithNotExistingMember() {
        // given
        given(tokenPort.parseRefreshToken(REFRESH_TOKEN)).willReturn(CLAIMS);
        given(readRefreshTokenPort.read(MEMBER_ID, SESSION_ID)).willReturn(Optional.of(REFRESH_TOKEN));
        given(readMemberPort.readByMemberId(MEMBER_ID)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> reissueAuthTokenUsecase.reissue(COMMAND));

        // then
        assertEquals(BusinessAuthErrorType.FAIL_REISSUE, exception.getErrorType());
        thenNotReissued();
    }

    private void givenReissuable() {
        given(tokenPort.parseRefreshToken(REFRESH_TOKEN)).willReturn(CLAIMS);
        given(readRefreshTokenPort.read(MEMBER_ID, SESSION_ID)).willReturn(Optional.of(REFRESH_TOKEN));
        given(readMemberPort.readByMemberId(MEMBER_ID)).willReturn(Optional.of(member()));
        given(tokenPort.issueAccessToken(eq(MEMBER_ID), eq(Role.MEMBER), eq(SESSION_ID), any(Instant.class)))
                .willReturn(ISSUED_ACCESS_TOKEN);
        given(tokenPort.issueRefreshToken(eq(MEMBER_ID), eq(SESSION_ID), any(Instant.class)))
                .willReturn(ISSUED_REFRESH_TOKEN);
    }

    private void thenNotReissued() {
        then(tokenPort).should().parseRefreshToken(REFRESH_TOKEN);
        then(tokenPort).shouldHaveNoMoreInteractions();
        then(saveRefreshTokenPort).shouldHaveNoInteractions();
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
