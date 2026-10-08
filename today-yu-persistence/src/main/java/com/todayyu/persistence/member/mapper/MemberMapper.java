package com.todayyu.persistence.member.mapper;

import com.todayyu.domain.member.entity.Account;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.vo.MemberId;
import com.todayyu.domain.member.vo.Nickname;
import com.todayyu.persistence.common.Mapper;
import com.todayyu.persistence.member.entity.MemberJpaEntity;

import java.util.List;

@Mapper
public class MemberMapper {
    /**
     * {@link Member}를 {@link MemberJpaEntity}로 변환하는 메서드
     *
     * @param domainEntity {@link Member} 객체
     * @return {@link MemberJpaEntity} 객체
     */
    public MemberJpaEntity toJpaEntity(Member domainEntity) {
        return MemberJpaEntity.builder()
                .email(domainEntity.getEmail().getValue())
                .nickname(domainEntity.getNickname().getValue())
                .role(domainEntity.getRole())
                .build();
    }
    
    /**
     * {@link MemberJpaEntity}를 {@link Member}로 변환하는 메서드
     *
     * @param jpaEntity {@link MemberJpaEntity} 객체
     * @param accounts 해당 멤버와 연결된 계정 {@link Account} 목록
     * @return {@link Member} 객체
     */
    public Member toDomain(MemberJpaEntity jpaEntity, List<Account> accounts) {
        return Member.withId(
                new MemberId(jpaEntity.getId()),
                new Email(jpaEntity.getEmail()),
                new Nickname(jpaEntity.getNickname()),
                jpaEntity.getRole(),
                accounts
        );
    }
}
