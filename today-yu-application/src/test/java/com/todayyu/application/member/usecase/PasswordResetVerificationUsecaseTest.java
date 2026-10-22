package com.todayyu.application.member.usecase;

import com.todayyu.application.member.dto.command.PasswordResetVerificationIssueCommand;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.application.member.port.read.ReadPasswordResetVerificationPort;
import com.todayyu.application.member.port.save.SavePasswordResetVerificationPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.entity.Account;
import com.todayyu.domain.member.entity.LocalAccount;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.entity.PasswordResetVerification;
import com.todayyu.domain.member.entity.SocialAccount;
import com.todayyu.domain.member.error.MemberErrorType;
import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.domain.member.vo.Role;
import com.todayyu.domain.member.vo.SocialIdentity;
import com.todayyu.domain.member.vo.VerificationCode;
import com.todayyu.domain.member.vo.VerificationFailCount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class PasswordResetVerificationUsecaseTest {
    private static final Email EMAIL = new Email("user@example.com");
    private static final PasswordResetVerificationIssueCommand COMMAND = new PasswordResetVerificationIssueCommand(EMAIL);
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final EncodedPassword ENCODED_PASSWORD = new EncodedPassword("encoded-password");

    @Mock
    private ReadPasswordResetVerificationPort readPasswordResetVerificationPort;

    @Mock
    private SavePasswordResetVerificationPort savePasswordResetVerificationPort;

    @Mock
    private ReadMemberPort readMemberPort;

    @InjectMocks
    private PasswordResetVerificationUsecase passwordResetVerificationUsecase;

    @Captor
    private ArgumentCaptor<PasswordResetVerification> verificationCaptor;

    @Test
    @DisplayName("로컬 계정 회원이고 진행 중인 인증이 없으면 6자리 인증 코드를 새로 발급해 저장한다")
    void issue() {
        given(readMemberPort.readByEmail(EMAIL)).willReturn(Optional.of(localMember()));
        given(readPasswordResetVerificationPort.readByEmail(EMAIL)).willReturn(Optional.empty());

        passwordResetVerificationUsecase.issue(COMMAND);

        then(savePasswordResetVerificationPort).should().save(verificationCaptor.capture());
        PasswordResetVerification saved = verificationCaptor.getValue();
        assertEquals(EMAIL, saved.getEmail());
        assertTrue(saved.getCode().getValue().matches("\\d{6}"));
        assertEquals(VerificationFailCount.zero(), saved.getFailCount());
    }

    @Test
    @DisplayName("발급 후 1분이 지났다면 인증 코드를 재발급해 저장한다")
    void reissue() {
        LocalDateTime previousIssuedAt = LocalDateTime.now().minusMinutes(2);
        PasswordResetVerification existing = PasswordResetVerification.issue(
                EMAIL, new VerificationCode("111111"), previousIssuedAt);
        given(readMemberPort.readByEmail(EMAIL)).willReturn(Optional.of(localMember()));
        given(readPasswordResetVerificationPort.readByEmail(EMAIL)).willReturn(Optional.of(existing));

        passwordResetVerificationUsecase.issue(COMMAND);

        then(savePasswordResetVerificationPort).should().save(verificationCaptor.capture());
        PasswordResetVerification saved = verificationCaptor.getValue();
        assertEquals(EMAIL, saved.getEmail());
        assertTrue(saved.getIssuedAt().isAfter(previousIssuedAt));
    }

    @Test
    @DisplayName("재발급하면 인증 실패 횟수를 0으로 초기화한다")
    void reissueResetsFailCount() {
        PasswordResetVerification existing = PasswordResetVerification.of(
                EMAIL, new VerificationCode("111111"), LocalDateTime.now().minusMinutes(2), VerificationFailCount.of(3));
        given(readMemberPort.readByEmail(EMAIL)).willReturn(Optional.of(localMember()));
        given(readPasswordResetVerificationPort.readByEmail(EMAIL)).willReturn(Optional.of(existing));

        passwordResetVerificationUsecase.issue(COMMAND);

        then(savePasswordResetVerificationPort).should().save(verificationCaptor.capture());
        assertEquals(VerificationFailCount.zero(), verificationCaptor.getValue().getFailCount());
    }

    @Test
    @DisplayName("발급 후 1분이 지나지 않았다면 재발급할 수 없고 저장하지 않는다")
    void reissueTooEarly() {
        PasswordResetVerification existing = PasswordResetVerification.issue(
                EMAIL, new VerificationCode("111111"), LocalDateTime.now());
        given(readMemberPort.readByEmail(EMAIL)).willReturn(Optional.of(localMember()));
        given(readPasswordResetVerificationPort.readByEmail(EMAIL)).willReturn(Optional.of(existing));

        CustomException exception = assertThrows(CustomException.class,
                () -> passwordResetVerificationUsecase.issue(COMMAND));

        assertEquals(MemberErrorType.VERIFICATION_CODE_REISSUE_TOO_EARLY, exception.getErrorType());
        then(savePasswordResetVerificationPort).should(never()).save(any());
    }

    @Test
    @DisplayName("가입되지 않은 이메일이면 예외 없이 종료하고 인증을 발급하지 않는다")
    void notRegisteredEmail() {
        given(readMemberPort.readByEmail(EMAIL)).willReturn(Optional.empty());

        passwordResetVerificationUsecase.issue(COMMAND);

        then(readPasswordResetVerificationPort).shouldHaveNoInteractions();
        then(savePasswordResetVerificationPort).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("로컬 계정이 없는 소셜 회원이면 예외 없이 종료하고 인증을 발급하지 않는다")
    void noLocalAccount() {
        given(readMemberPort.readByEmail(EMAIL)).willReturn(Optional.of(socialOnlyMember()));

        passwordResetVerificationUsecase.issue(COMMAND);

        then(readPasswordResetVerificationPort).shouldHaveNoInteractions();
        then(savePasswordResetVerificationPort).shouldHaveNoInteractions();
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
