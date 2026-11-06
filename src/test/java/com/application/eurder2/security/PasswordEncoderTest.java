package com.application.eurder2.security;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordEncoderTest {

    private final PasswordEncoder encoder = new SecurityConfig().passwordEncoder();
    private static final String RAW = "Password1!";

    @Test
    void encode_doesNotReturnRawPassword() {
        assertThat(encoder.encode(RAW)).isNotEqualTo(RAW).doesNotContain(RAW);
    }

    @Test
    void encode_producesBCryptHash() {
        assertThat(encoder.encode(RAW)).matches("^\\$2[aby]?\\$\\d{2}\\$.{53}$");
    }

    @Test
    void encode_samePasswordTwice_givesDifferentHashes() {
        assertThat(encoder.encode(RAW)).isNotEqualTo(encoder.encode(RAW));
    }

    @Test
    void matches_correctPassword_isTrue() {
        assertThat(encoder.matches(RAW, encoder.encode(RAW))).isTrue();
    }

    @Test
    void matches_wrongPassword_isFalse() {
        assertThat(encoder.matches("Password2!", encoder.encode(RAW))).isFalse();
    }

    @Test
    void matches_isCaseSensitive() {
        assertThat(encoder.matches("password1!", encoder.encode(RAW))).isFalse();
    }

    @Test
    void matches_rawPasswordAgainstItself_isFalse() {
        assertThat(encoder.matches(RAW, RAW)).isFalse();
    }
}