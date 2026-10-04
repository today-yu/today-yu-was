package com.todayyu.infrastructure.member.adapter.password;

import com.todayyu.application.member.port.PasswordPort;
import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.Password;
import com.todayyu.infrastructure.common.annotation.Adapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Adapter
public class PasswordAdapter implements PasswordPort {
    private final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private final String dummyPassword;
    
    public PasswordAdapter() {
        this.dummyPassword = encoder.encode(UUID.randomUUID().toString());
    }
    
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
    
    /**
     * 비밀번호 일치 여부를 확인하는 메서드
     *
     * @param rawPassword 비밀번호 평문
     * @param encodedPassword 암호화된 비밀번호
     * @return 비밀번호 일치 여부
     */
    @Override
    public boolean matches(Password rawPassword, EncodedPassword encodedPassword) {
        return encoder.matches(rawPassword.getValue(), encodedPassword.getValue());
    }
    
    /**
     * 더미 비밀번호와의 일치 여부를 확인하는 메서드
     *
     * <p> '인증 실패'와 같이 비밀번호 일치 여부 확인 과정이 필요하지만 항상 {@code false}를 반환해야할 때 사용한다. </p>
     *
     * @param rawPassword 비밀번호 평문
     * @return 비밀번호 일치 여부 - 항상 {@code false} 반환
     */
    @Override
    public boolean matchesDummy(Password rawPassword) {
        return encoder.matches(rawPassword.getValue(), dummyPassword);
    }
}
