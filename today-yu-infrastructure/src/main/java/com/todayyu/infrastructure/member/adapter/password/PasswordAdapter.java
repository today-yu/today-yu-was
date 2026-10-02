package com.todayyu.infrastructure.member.adapter.password;

import com.todayyu.application.member.port.PasswordPort;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.Password;
import com.todayyu.infrastructure.common.annotation.Adapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Adapter
public class PasswordAdapter implements PasswordPort {
    private final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    
    /**
     * 비밀번호를 인코딩하는 메서드
     *
     * @param password 비밀번호 평문
     * @return 암호화된 비밀번호
     */
    @Override
    public EncodedPassword encode(Password password) {
        return new EncodedPassword(encoder.encode(password.getValue()));
    }
}
