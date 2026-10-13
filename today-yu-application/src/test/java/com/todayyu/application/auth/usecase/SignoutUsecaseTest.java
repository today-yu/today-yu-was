package com.todayyu.application.auth.usecase;

import com.todayyu.application.auth.dto.AccessTokenClaims;
import com.todayyu.application.auth.dto.command.SignoutCommand;
import com.todayyu.application.auth.error.BusinessAuthErrorType;
import com.todayyu.application.auth.port.TokenPort;
import com.todayyu.application.auth.port.save.SaveBlacklistTokenPort;
import com.todayyu.application.auth.port.save.SaveRefreshTokenPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.auth.vo.AccessToken;
import com.todayyu.domain.auth.vo.SessionId;
import com.todayyu.domain.auth.vo.TokenId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SignoutUsecaseTest {
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final TokenId TOKEN_ID = new TokenId("token-id");
    private static final SessionId SESSION_ID = SessionId.create();
    private static final Instant EXPIRES_AT = Instant.parse("2026-10-04T00:30:00Z");
    private static final AccessToken ACCESS_TOKEN = new AccessToken("access-token");
    private static final SignoutCommand COMMAND = new SignoutCommand(ACCESS_TOKEN);
    private static final AccessTokenClaims CLAIMS =
            new AccessTokenClaims(MEMBER_ID, TOKEN_ID, SESSION_ID, Role.MEMBER, EXPIRES_AT);

    @Mock
    private TokenPort tokenPort;

    @Mock
    private SaveBlacklistTokenPort saveBlacklistTokenPort;

    @Mock
    private SaveRefreshTokenPort saveRefreshTokenPort;

    @InjectMocks
    private SignoutUsecase signoutUsecase;

    @Test
    @DisplayName("로그아웃하면 Access Token을 만료 기한까지 블랙리스트에 등록하고 해당 세션의 Refresh Token을 삭제한다")
    void signout() {
        // given
        given(tokenPort.parseAccessToken(ACCESS_TOKEN)).willReturn(CLAIMS);

        // when
        signoutUsecase.signout(COMMAND);

        // then
        then(saveBlacklistTokenPort).should().save(TOKEN_ID, EXPIRES_AT);
        then(saveRefreshTokenPort).should().delete(MEMBER_ID, SESSION_ID);
    }

    @Test
    @DisplayName("Access Token 파싱에 실패하면 예외를 그대로 전파하고 블랙리스트 등록과 Refresh Token 삭제를 하지 않는다")
    void signoutWithInvalidToken() {
        // given
        given(tokenPort.parseAccessToken(ACCESS_TOKEN))
                .willThrow(new CustomException(BusinessAuthErrorType.EXPIRED_TOKEN));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> signoutUsecase.signout(COMMAND));

        // then
        assertEquals(BusinessAuthErrorType.EXPIRED_TOKEN, exception.getErrorType());
        then(saveBlacklistTokenPort).should(never()).save(any(), any());
        then(saveRefreshTokenPort).should(never()).delete(any(), any());
    }
}
