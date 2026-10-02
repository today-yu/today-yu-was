package com.todayyu.persistence.member.adapter;

import com.todayyu.application.member.port.read.ReadMemberPort;
import com.todayyu.application.member.port.save.SaveMemberPort;
import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.member.vo.Email;
import com.todayyu.persistence.common.PersistenceAdapter;
import com.todayyu.persistence.member.entity.AccountJpaEntity;
import com.todayyu.persistence.member.entity.MemberJpaEntity;
import com.todayyu.persistence.member.mapper.AccountMapper;
import com.todayyu.persistence.member.mapper.MemberMapper;
import com.todayyu.persistence.member.repository.AccountJpaRepository;
import com.todayyu.persistence.member.repository.MemberJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@PersistenceAdapter
@RequiredArgsConstructor
public class JpaMemberAdapter implements ReadMemberPort, SaveMemberPort {
    private final MemberJpaRepository memberRepository;
    private final AccountJpaRepository accountRepository;
    private final MemberMapper memberMapper;
    private final AccountMapper accountMapper;
    
    /**
     * 이메일({@link Email})을 통해 회원 존재 여부를 반환하는 메서드
     *
     * @param email 이메일 객체
     * @return 회원 존재 여부
     */
    @Override
    public boolean existsByEmail(Email email) {
        return memberRepository.existsByEmail(email.getValue());
    }
    
    /**
     * 새로운 회원을 저장하는 메서드
     *
     * @param member 회원 객체
     * @return 새로 저장된 회원 객체
     */
    @Override
    @Transactional
    public Member save(Member member) {
        MemberJpaEntity savedMember = memberRepository.save(memberMapper.toJpaEntity(member));
        List<AccountJpaEntity> savedAccounts = accountRepository.saveAll(toAccountJpaEntities(member, savedMember.getId()));
        
        return toDomain(savedMember, savedAccounts);
    }
    
    /**
     * 회원, 계정 JPA 엔티티({@link MemberJpaEntity}, {@link AccountJpaEntity})를
     * 회원, 계정 도메인 엔티티({@link Member}, {@link com.todayyu.domain.member.entity.Account})로 변환하는 메서드
     *
     * @param member 회원 JPA 엔티티 객체
     * @param accounts 계정 JPA 엔티티 객체 목록
     * @return 회원 도메인 엔티티 객체
     */
    private Member toDomain(MemberJpaEntity member, List<AccountJpaEntity> accounts) {
        return memberMapper.toDomain(member, accounts.stream().map(accountMapper::toDomain).toList());
    }
    
    /**
     * 회원의 계정 도메인 엔티티({@link Member#getAccounts()})를
     * 계정 JPA 엔티티({@link  AccountJpaEntity}) 목록으로 변환하는 메서드
     *
     * @param member 멤버 도메인 엔티티 객체
     * @param memberId 멤버 아이디
     * @return 회원의 계정 JPA 엔티티 객체 목록
     */
    private List<AccountJpaEntity> toAccountJpaEntities(Member member, Long memberId) {
        return member.getAccounts().stream()
                .map(account -> accountMapper.toJpaEntity(account, memberId))
                .toList();
    }
}
