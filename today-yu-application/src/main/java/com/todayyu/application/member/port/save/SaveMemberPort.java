package com.todayyu.application.member.port.save;

import com.todayyu.domain.member.entity.Member;

public interface SaveMemberPort {
    Member save(Member member);
}
