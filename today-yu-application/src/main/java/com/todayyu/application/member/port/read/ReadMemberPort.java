package com.todayyu.application.member.port.read;

import com.todayyu.domain.member.entity.Member;
import com.todayyu.domain.common.vo.Email;
import com.todayyu.domain.member.vo.MemberId;

import java.util.Optional;

public interface ReadMemberPort {
    Optional<Member> readByMemberId(MemberId memberId);
    Optional<Member> readByEmail(Email email);
    boolean existsByEmail(Email email);
}
