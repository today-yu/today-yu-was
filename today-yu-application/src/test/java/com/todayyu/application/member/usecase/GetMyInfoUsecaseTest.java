package com.todayyu.application.member.usecase;

import com.todayyu.application.member.dto.query.GetMyInfoQuery;
import com.todayyu.application.member.dto.result.MyInfoResult;
import com.todayyu.application.member.error.BusinessMemberErrorType;
import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.common.exception.CustomException;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.entity.LocalAccount;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.vo.AccountId;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetMyInfoUsecaseTest {
    private static final MemberId MEMBER_ID = new MemberId(1L);
    private static final Email EMAIL = new Email("user@example.com");
    private static final Nickname NICKNAME = new Nickname("명랑한 영대인 1");

    @Mock
    private ReadMemberPort readMemberPort;

    @InjectMocks
    private GetMyInfoUsecase getMyInfoUsecase;

    @Test
    @DisplayName("회원이 존재하면 회원 ID, 닉네임, 이메일을 반환한다")
    void getMyInfo() {
        // given
        given(readMemberPort.readByMemberId(MEMBER_ID)).willReturn(Optional.of(member()));

        // when
        MyInfoResult result = getMyInfoUsecase.getMyInfo(new GetMyInfoQuery(MEMBER_ID));

        // then
        assertEquals(MEMBER_ID, result.memberId());
        assertEquals(NICKNAME, result.nickname());
        assertEquals(EMAIL, result.email());
    }

    @Test
    @DisplayName("회원이 존재하지 않으면 NOT_FOUND 예외가 발생한다")
    void getMyInfoWithNotExistingMember() {
        // given
        given(readMemberPort.readByMemberId(MEMBER_ID)).willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () -> getMyInfoUsecase.getMyInfo(new GetMyInfoQuery(MEMBER_ID)));

        // then
        assertEquals(BusinessMemberErrorType.NOT_FOUND, exception.getErrorType());
    }

    private Member member() {
        return Member.withId(
                MEMBER_ID,
                EMAIL,
                NICKNAME,
                Role.MEMBER,
                List.of(LocalAccount.withId(new AccountId(1L), new EncodedPassword("encoded-password")))
        );
    }
}
