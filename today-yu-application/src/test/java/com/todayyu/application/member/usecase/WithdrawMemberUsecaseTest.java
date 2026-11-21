package com.todayyu.application.member.usecase;

import com.todayyu.application.member.dto.command.WithdrawMemberCommand;
import com.todayyu.application.member.error.BusinessMemberErrorType;
import com.todayyu.application.member.event.MemberWithdrawalEvent;
import com.todayyu.application.member.port.PasswordPort;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.application.member.port.save.SaveMemberPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.entity.Account;
import com.todayyu.domain.member.entity.LocalAccount;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.entity.SocialAccount;
import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
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
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class WithdrawMemberUsecaseTest {
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final Email EMAIL = new Email("user@example.com");
    private static final Nickname NICKNAME = new Nickname("영대생");
    private static final Password PASSWORD = new Password("password1234");
    private static final EncodedPassword ENCODED_PASSWORD = new EncodedPassword("encoded-password");
    private static final SocialIdentity SOCIAL_IDENTITY =
            new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId("kakao-12345"));

    @Mock
    private ReadMemberPort readMemberPort;

    @Mock
    private PasswordPort passwordPort;

    @Mock
    private SaveMemberPort saveMemberPort;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WithdrawMemberUsecase withdrawMemberUsecase;

    @Captor
    private ArgumentCaptor<Member> memberCaptor;

    @Captor
    private ArgumentCaptor<MemberWithdrawalEvent> eventCaptor;

    @Test
    @DisplayName("로컬 회원은 비밀번호가 일치하면 비식별화하여 탈퇴 처리하고 탈퇴 이벤트를 발행한다")
    void withdrawLocalMember() {
        // given
        given(readMemberPort.readByMemberId(MEMBER_ID)).willReturn(Optional.of(memberWith(localAccount())));
        given(passwordPort.matches(PASSWORD, ENCODED_PASSWORD)).willReturn(true);

        // when
        withdrawMemberUsecase.withdraw(new WithdrawMemberCommand(MEMBER_ID, PASSWORD));

        // then
        then(saveMemberPort).should().withdraw(memberCaptor.capture());
        Member withdrawn = memberCaptor.getValue();
        assertTrue(withdrawn.isWithdrawn());
        assertNull(withdrawn.getEmail());
        assertTrue(withdrawn.getAccounts().isEmpty());
        assertEquals(NICKNAME, withdrawn.getNickname());

        then(eventPublisher).should().publishEvent(eventCaptor.capture());
        MemberWithdrawalEvent event = eventCaptor.getValue();
        assertNotNull(event.eventId());
        assertEquals(MEMBER_ID, event.memberId());
        assertTrue(event.socialIdentities().isEmpty());
    }

    @Test
    @DisplayName("소셜 계정만 가진 회원은 비밀번호 검증 없이 탈퇴하며, 탈퇴 전 소셜 계정 정보를 이벤트에 담는다")
    void withdrawSocialMember() {
        // given
        given(readMemberPort.readByMemberId(MEMBER_ID)).willReturn(Optional.of(memberWith(socialAccount())));

        // when
        withdrawMemberUsecase.withdraw(new WithdrawMemberCommand(MEMBER_ID, null));

        // then
        then(passwordPort).should(never()).matches(any(), any());

        then(saveMemberPort).should().withdraw(memberCaptor.capture());
        assertTrue(memberCaptor.getValue().isWithdrawn());

        then(eventPublisher).should().publishEvent(eventCaptor.capture());
        assertEquals(List.of(SOCIAL_IDENTITY), eventCaptor.getValue().socialIdentities());
    }

    @Test
    @DisplayName("로컬 계정과 소셜 계정을 모두 가진 회원은 비밀번호 검증 후 탈퇴하며, 소셜 계정 정보만 이벤트에 담는다")
    void withdrawMemberWithLocalAndSocialAccounts() {
        // given
        given(readMemberPort.readByMemberId(MEMBER_ID))
                .willReturn(Optional.of(memberWith(localAccount(), socialAccount())));
        given(passwordPort.matches(PASSWORD, ENCODED_PASSWORD)).willReturn(true);

        // when
        withdrawMemberUsecase.withdraw(new WithdrawMemberCommand(MEMBER_ID, PASSWORD));

        // then
        then(saveMemberPort).should().withdraw(memberCaptor.capture());
        assertTrue(memberCaptor.getValue().getAccounts().isEmpty());

        then(eventPublisher).should().publishEvent(eventCaptor.capture());
        assertEquals(List.of(SOCIAL_IDENTITY), eventCaptor.getValue().socialIdentities());
    }

    @Test
    @DisplayName("회원이 존재하지 않으면 NOT_FOUND 예외가 발생하고 탈퇴 처리와 이벤트 발행을 하지 않는다")
    void withdrawWithNotFoundMember() {
        // given
        given(readMemberPort.readByMemberId(MEMBER_ID)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> withdrawMemberUsecase.withdraw(new WithdrawMemberCommand(MEMBER_ID, PASSWORD)));

        // then
        assertEquals(BusinessMemberErrorType.NOT_FOUND, exception.getErrorType());
        then(saveMemberPort).should(never()).withdraw(any());
        then(eventPublisher).should(never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("로컬 회원이 비밀번호를 입력하지 않으면 PASSWORD_REQUIRED 예외가 발생하고 탈퇴 처리와 이벤트 발행을 하지 않는다")
    void withdrawLocalMemberWithoutPassword() {
        // given
        given(readMemberPort.readByMemberId(MEMBER_ID)).willReturn(Optional.of(memberWith(localAccount())));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> withdrawMemberUsecase.withdraw(new WithdrawMemberCommand(MEMBER_ID, null)));

        // then
        assertEquals(BusinessMemberErrorType.PASSWORD_REQUIRED, exception.getErrorType());
        then(passwordPort).should(never()).matches(any(), any());
        then(saveMemberPort).should(never()).withdraw(any());
        then(eventPublisher).should(never()).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("로컬 회원의 비밀번호가 일치하지 않으면 CURRENT_PASSWORD_MISMATCH 예외가 발생하고 탈퇴 처리와 이벤트 발행을 하지 않는다")
    void withdrawLocalMemberWithMismatchedPassword() {
        // given
        given(readMemberPort.readByMemberId(MEMBER_ID)).willReturn(Optional.of(memberWith(localAccount())));
        given(passwordPort.matches(PASSWORD, ENCODED_PASSWORD)).willReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> withdrawMemberUsecase.withdraw(new WithdrawMemberCommand(MEMBER_ID, PASSWORD)));

        // then
        assertEquals(BusinessMemberErrorType.CURRENT_PASSWORD_MISMATCH, exception.getErrorType());
        then(saveMemberPort).should(never()).withdraw(any());
        then(eventPublisher).should(never()).publishEvent(any(Object.class));
    }

    private Member memberWith(Account... accounts) {
        return Member.withId(MEMBER_ID, EMAIL, NICKNAME, Role.MEMBER, List.of(accounts));
    }

    private LocalAccount localAccount() {
        return LocalAccount.withId(new AccountId(10L), ENCODED_PASSWORD);
    }

    private SocialAccount socialAccount() {
        return SocialAccount.withId(new AccountId(11L), SOCIAL_IDENTITY);
    }
}
