package com.application.eurder2.security;

import com.application.eurder2.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SecurityServiceVerifyPasswordTest {

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecurityService securityService = new SecurityService(mock(UserRepository.class), encoder);
    private final String hash = encoder.encode("Password1!");

    @Test
    void correctPassword_isTrue() {
        assertThat(securityService.verifyPassword("Password1!", hash)).isTrue();
    }

    @Test
    void wrongPassword_isFalse() {
        assertThat(securityService.verifyPassword("Wrong1!xx", hash)).isFalse();
    }

    @Test
    void nullRawPassword_isFalse() {
        assertThat(securityService.verifyPassword(null, hash)).isFalse();
    }

    @Test
    void nullHash_isFalse() {
        assertThat(securityService.verifyPassword("Password1!", null)).isFalse();
    }

    @Test
    void emptyPassword_isFalse() {
        assertThat(securityService.verifyPassword("", hash)).isFalse();
    }
}