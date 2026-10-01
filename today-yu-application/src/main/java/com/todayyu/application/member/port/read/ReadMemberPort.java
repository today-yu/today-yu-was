package com.todayyu.application.member.port.read;

import com.todayyu.domain.member.vo.Email;

public interface ReadMemberPort {
    boolean existsByEmail(Email email);
}
