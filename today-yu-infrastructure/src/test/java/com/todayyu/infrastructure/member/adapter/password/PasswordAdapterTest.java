package com.todayyu.infrastructure.member.adapter.password;

import com.todayyu.domain.member.vo.EncodedPassword;
import com.todayyu.domain.member.vo.Password;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordAdapterTest {
    private static final Password PASSWORD = new Password("password1234");

    private final PasswordAdapter passwordAdapter = new PasswordAdapter();
    private final PasswordEncoder verifier = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Test
    @DisplayName("비밀번호를 BCrypt 알고리즘 식별자가 붙은 값으로 인코딩한다")
    void encodeWithBcrypt() {
        // when
        EncodedPassword encoded = passwordAdapter.encode(PASSWORD);

        // then
        assertTrue(encoded.getValue().startsWith("{bcrypt}"));
    }

    @Test
    @DisplayName("인코딩한 비밀번호는 원본 비밀번호와 일치하는지 검증할 수 있다")
    void encodedPasswordMatchesRawPassword() {
        // when
        EncodedPassword encoded = passwordAdapter.encode(PASSWORD);

        // then
        assertNotEquals(PASSWORD.getValue(), encoded.getValue());
        assertTrue(verifier.matches(PASSWORD.getValue(), encoded.getValue()));
    }

    @Test
    @DisplayName("같은 비밀번호라도 인코딩할 때마다 다른 값이 생성된다")
    void encodeWithRandomSalt() {
        // when
        EncodedPassword first = passwordAdapter.encode(PASSWORD);
        EncodedPassword second = passwordAdapter.encode(PASSWORD);

        // then
        assertNotEquals(first, second);
    }
}
