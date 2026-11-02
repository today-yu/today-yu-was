package com.todayyu.application.member.usecase;

import com.todayyu.application.member.port.NicknamePort;
import com.todayyu.application.member.port.save.SaveMemberPort;
import com.todayyu.domain.member.entity.Account;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.entity.SocialAccount;
import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.member.vo.AuthProvider;
import com.todayyu.domain.member.vo.AuthProviderUserId;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.domain.member.vo.NicknameModifier;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class SocialSignupUsecaseTest {
    private static final SocialIdentity SOCIAL_IDENTITY =
            new SocialIdentity(AuthProvider.KAKAO, new AuthProviderUserId("kakao-user-id"));

    @Mock
    private SaveMemberPort saveMemberPort;

    @Mock
    private NicknamePort nicknamePort;

    @InjectMocks
    private SocialSignupUsecase socialSignupUsecase;

    @Captor
    private ArgumentCaptor<Member> memberCaptor;

    @Captor
    private ArgumentCaptor<NicknameModifier> modifierCaptor;

    @Test
    @DisplayName("인증 제공자 내 사용자 식별 정보로 이메일 없이 소셜 계정을 가진 회원을 저장한다")
    void signup() {
        // given
        given(nicknamePort.issueNext(any())).willReturn(1L);

        // when
        socialSignupUsecase.signup(SOCIAL_IDENTITY);

        // then
        then(saveMemberPort).should().save(memberCaptor.capture());
        Member saved = memberCaptor.getValue();
        assertNull(saved.getEmail());
        assertEquals(Role.MEMBER, saved.getRole());
        assertEquals(1, saved.getAccounts().size());

        Account account = saved.getAccounts().get(0);
        SocialAccount socialAccount = assertInstanceOf(SocialAccount.class, account);
        assertEquals(AuthProvider.KAKAO, socialAccount.getAuthProvider());
        assertEquals(SOCIAL_IDENTITY, socialAccount.getSocialIdentity());
    }

    @Test
    @DisplayName("선택된 수식어로 발급받은 번호를 사용해 '{수식어} 영대인 {번호}' 형식의 닉네임을 부여한다")
    void signupWithGeneratedNickname() {
        // given
        given(nicknamePort.issueNext(any())).willReturn(7L);

        // when
        socialSignupUsecase.signup(SOCIAL_IDENTITY);

        // then
        then(nicknamePort).should().issueNext(modifierCaptor.capture());
        then(saveMemberPort).should().save(memberCaptor.capture());
        NicknameModifier modifier = modifierCaptor.getValue();
        assertEquals(modifier.getValue() + " 영대인 7", memberCaptor.getValue().getNickname().getValue());
    }

    @Test
    @DisplayName("저장된 회원을 반환한다")
    void signupReturnsSavedMember() {
        // given
        Member savedMember = Member.withId(
                new MemberId(1L),
                null,
                new Nickname("명랑한 영대인 1"),
                Role.MEMBER,
                List.of(SocialAccount.withId(new AccountId(1L), SOCIAL_IDENTITY))
        );
        given(nicknamePort.issueNext(any())).willReturn(1L);
        given(saveMemberPort.save(any())).willReturn(savedMember);

        // when
        Member result = socialSignupUsecase.signup(SOCIAL_IDENTITY);

        // then
        assertSame(savedMember, result);
    }
}
