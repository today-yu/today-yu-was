package com.todayyu.persistence.member.mapper;

import com.todayyu.domain.member.entity.Account;
import com.todayyu.domain.member.entity.LocalAccount;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.vo.AccountId;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.domain.member.vo.Role;
import com.todayyu.persistence.member.entity.MemberJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MemberMapperTest {
    private static final Long MEMBER_ID = 1L;
    private static final String EMAIL = "user@yu.ac.kr";
    private static final String NICKNAME = "착한 영대인 1";

    private final MemberMapper memberMapper = new MemberMapper();

    @Test
    @DisplayName("회원을 이메일, 닉네임, 역할을 가진 회원 JPA 엔티티로 변환한다")
    void toJpaEntity() {
        // given
        LocalAccount account = LocalAccount.withoutId(new EncodedPassword("encoded-password"));
        Member member = Member.withoutId(new Email(EMAIL), new Nickname(NICKNAME), Role.MEMBER, account);

        // when
        MemberJpaEntity jpaEntity = memberMapper.toJpaEntity(member);

        // then
        assertNull(jpaEntity.getId());
        assertEquals(EMAIL, jpaEntity.getEmail());
        assertEquals(NICKNAME, jpaEntity.getNickname());
        assertEquals(Role.MEMBER, jpaEntity.getRole());
    }

    @Test
    @DisplayName("회원 JPA 엔티티와 계정 목록을 아이디를 가진 회원으로 변환한다")
    void toDomain() {
        // given
        MemberJpaEntity jpaEntity = MemberJpaEntity.builder()
                .email(EMAIL)
                .nickname(NICKNAME)
                .role(Role.MEMBER)
                .build();
        ReflectionTestUtils.setField(jpaEntity, "id", MEMBER_ID);
        List<Account> accounts = List.of(
                LocalAccount.withId(new AccountId(10L), new EncodedPassword("encoded-password")));

        // when
        Member member = memberMapper.toDomain(jpaEntity, accounts);

        // then
        assertEquals(new MemberId(MEMBER_ID), member.getId());
        assertEquals(new Email(EMAIL), member.getEmail());
        assertEquals(new Nickname(NICKNAME), member.getNickname());
        assertEquals(Role.MEMBER, member.getRole());
        assertEquals(accounts, member.getAccounts());
    }
}
