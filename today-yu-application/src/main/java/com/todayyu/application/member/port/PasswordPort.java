package com.todayyu.application.member.port;

import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.Password;

public interface PasswordPort {
    EncodedPassword encode(Password password);
}
