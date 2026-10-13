package com.todayyu.application.member.usecase;

import com.todayyu.application.member.dto.command.SignupCommand;
import com.todayyu.application.member.error.BusinessMemberErrorType;
import com.todayyu.application.member.port.NicknamePort;
import com.todayyu.application.member.port.PasswordPort;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.application.member.port.save.SaveMemberPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.member.entity.Account;
import com.todayyu.domain.member.entity.LocalAccount;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.NicknameModifier;
import com.todayyu.domain.member.vo.Password;
import com.todayyu.domain.member.vo.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class SignupUsecaseTest {
    private static final Email EMAIL = new Email("user@example.com");
    private static final Password PASSWORD = new Password("password1234");
    private static final EncodedPassword ENCODED_PASSWORD = new EncodedPassword("encoded-password");
    private static final SignupCommand COMMAND = new SignupCommand(EMAIL, PASSWORD);

    @Mock
    private ReadMemberPort readMemberPort;

    @Mock
    private SaveMemberPort saveMemberPort;

    @Mock
    private PasswordPort passwordPort;

    @Mock
    private NicknamePort nicknamePort;

    @InjectMocks
    private SignupUsecase signupUsecase;

    @Captor
    private ArgumentCaptor<Member> memberCaptor;

    @Captor
    private ArgumentCaptor<NicknameModifier> modifierCaptor;

    @Test
    @DisplayName("가입되지 않은 이메일이면 인코딩된 비밀번호의 로컬 계정을 가진 회원을 저장한다")
    void signup() {
        // given
        given(readMemberPort.existsByEmail(EMAIL)).willReturn(false);
        given(passwordPort.encode(PASSWORD)).willReturn(ENCODED_PASSWORD);
        given(nicknamePort.issueNext(any())).willReturn(1L);

        // when
        signupUsecase.signup(COMMAND);

        // then
        then(saveMemberPort).should().save(memberCaptor.capture());
        Member saved = memberCaptor.getValue();
        assertEquals(EMAIL, saved.getEmail());
        assertEquals(Role.MEMBER, saved.getRole());
        assertEquals(1, saved.getAccounts().size());

        Account account = saved.getAccounts().get(0);
        LocalAccount localAccount = assertInstanceOf(LocalAccount.class, account);
        assertEquals(AuthProvider.LOCAL, localAccount.getAuthProvider());
        assertEquals(ENCODED_PASSWORD, localAccount.getPassword());
    }

    @Test
    @DisplayName("선택된 수식어로 발급받은 번호를 사용해 '{수식어} 영대인 {번호}' 형식의 닉네임을 부여한다")
    void signupWithGeneratedNickname() {
        // given
        given(readMemberPort.existsByEmail(EMAIL)).willReturn(false);
        given(passwordPort.encode(PASSWORD)).willReturn(ENCODED_PASSWORD);
        given(nicknamePort.issueNext(any())).willReturn(7L);

        // when
        signupUsecase.signup(COMMAND);

        // then
        then(nicknamePort).should().issueNext(modifierCaptor.capture());
        then(saveMemberPort).should().save(memberCaptor.capture());
        NicknameModifier modifier = modifierCaptor.getValue();
        assertEquals(modifier.getValue() + " 영대인 7", memberCaptor.getValue().getNickname().getValue());
    }

    @Test
    @DisplayName("이미 가입된 이메일이면 가입에 실패하고 비밀번호 인코딩, 닉네임 발급, 저장을 하지 않는다")
    void signupWithDuplicatedEmail() {
        // given
        given(readMemberPort.existsByEmail(EMAIL)).willReturn(true);

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> signupUsecase.signup(COMMAND));

        // then
        assertEquals(BusinessMemberErrorType.DUPLICATED_EMAIL, exception.getErrorType());
        then(passwordPort).should(never()).encode(any());
        then(nicknamePort).should(never()).issueNext(any());
        then(saveMemberPort).should(never()).save(any());
    }
}
