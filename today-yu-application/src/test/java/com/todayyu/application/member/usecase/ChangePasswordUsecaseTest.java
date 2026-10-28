package com.todayyu.application.member.usecase;

import com.todayyu.application.member.dto.command.ChangePasswordCommand;
import com.todayyu.application.member.error.BusinessMemberErrorType;
import com.todayyu.application.member.port.PasswordPort;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.application.member.port.save.SaveMemberPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ChangePasswordUsecaseTest {
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final Email EMAIL = new Email("user@example.com");
    private static final Nickname NICKNAME = new Nickname("영대생");
    private static final Password CURRENT_PASSWORD = new Password("current1234");
    private static final Password NEW_PASSWORD = new Password("new-password1234");
    private static final EncodedPassword ENCODED_CURRENT_PASSWORD = new EncodedPassword("encoded-current-password");
    private static final EncodedPassword ENCODED_NEW_PASSWORD = new EncodedPassword("encoded-new-password");
    private static final ChangePasswordCommand COMMAND = new ChangePasswordCommand(MEMBER_ID, CURRENT_PASSWORD, NEW_PASSWORD);

    @Mock
    private PasswordPort passwordPort;

    @Mock
    private ReadMemberPort readMemberPort;

    @Mock
    private SaveMemberPort saveMemberPort;

    @InjectMocks
    private ChangePasswordUsecase changePasswordUsecase;

    @Captor
    private ArgumentCaptor<Member> memberCaptor;

    @Test
    @DisplayName("현재 비밀번호가 일치하면 새 비밀번호를 암호화하여 회원의 비밀번호를 변경한다")
    void change() {
        // given
        given(readMemberPort.readByMemberId(MEMBER_ID)).willReturn(Optional.of(localMember()));
        given(passwordPort.matches(CURRENT_PASSWORD, ENCODED_CURRENT_PASSWORD)).willReturn(true);
        given(passwordPort.encode(NEW_PASSWORD)).willReturn(ENCODED_NEW_PASSWORD);

        // when
        changePasswordUsecase.change(COMMAND);

        // then
        then(saveMemberPort).should().update(memberCaptor.capture());
        Member updated = memberCaptor.getValue();
        assertEquals(MEMBER_ID, updated.getId());
        assertEquals(Optional.of(ENCODED_NEW_PASSWORD), updated.getEncodedPassword());
    }

    @Test
    @DisplayName("회원이 존재하지 않으면 NOT_FOUND 예외가 발생하고 비밀번호를 변경하지 않는다")
    void changeWithNotFoundMember() {
        // given
        given(readMemberPort.readByMemberId(MEMBER_ID)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> changePasswordUsecase.change(COMMAND));

        // then
        assertEquals(BusinessMemberErrorType.NOT_FOUND, exception.getErrorType());
        then(passwordPort).should(never()).matches(any(), any());
        then(passwordPort).should(never()).encode(any());
        then(saveMemberPort).should(never()).update(any());
    }

    @Test
    @DisplayName("로컬 계정이 없는 회원이면 NO_LOCAL_ACCOUNT 예외가 발생하고 비밀번호를 변경하지 않는다")
    void changeWithoutLocalAccount() {
        // given
        given(readMemberPort.readByMemberId(MEMBER_ID)).willReturn(Optional.of(socialMember()));

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> changePasswordUsecase.change(COMMAND));

        // then
        assertEquals(BusinessMemberErrorType.NO_LOCAL_ACCOUNT, exception.getErrorType());
        then(passwordPort).should(never()).matches(any(), any());
        then(passwordPort).should(never()).encode(any());
        then(saveMemberPort).should(never()).update(any());
    }

    @Test
    @DisplayName("현재 비밀번호가 일치하지 않으면 CURRENT_PASSWORD_MISMATCH 예외가 발생하고 비밀번호를 변경하지 않는다")
    void changeWithMismatchedCurrentPassword() {
        // given
        given(readMemberPort.readByMemberId(MEMBER_ID)).willReturn(Optional.of(localMember()));
        given(passwordPort.matches(CURRENT_PASSWORD, ENCODED_CURRENT_PASSWORD)).willReturn(false);

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> changePasswordUsecase.change(COMMAND));

        // then
        assertEquals(BusinessMemberErrorType.CURRENT_PASSWORD_MISMATCH, exception.getErrorType());
        then(passwordPort).should(never()).encode(any());
        then(saveMemberPort).should(never()).update(any());
    }

    private Member localMember() {
        LocalAccount account = LocalAccount.withId(new AccountId(10L), ENCODED_CURRENT_PASSWORD);
        return Member.withId(MEMBER_ID, EMAIL, NICKNAME, Role.MEMBER, List.of(account));
    }

    private Member socialMember() {
        SocialIdentity identity = new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId("kakao-12345"));
        SocialAccount account = SocialAccount.withId(new AccountId(10L), identity);
        return Member.withId(MEMBER_ID, EMAIL, NICKNAME, Role.MEMBER, List.of(account));
    }
}
