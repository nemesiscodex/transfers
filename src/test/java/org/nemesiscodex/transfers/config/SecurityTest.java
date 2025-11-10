package org.nemesiscodex.transfers.config;

import org.junit.jupiter.api.Test;
import org.nemesiscodex.transfers.core.security.JwtAuthenticationWebFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestContainers.class)
@SpringBootTest
class SecurityTest {

    @Autowired
    private SecurityWebFilterChain securityWebFilterChain;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtAuthenticationWebFilter jwtAuthenticationWebFilter;

    @Test
    void shouldHavePasswordEncoderBean() {
        assertThat(passwordEncoder).isNotNull();
    }

    @Test
    void shouldHaveSecurityWebFilterChainBean() {
        assertThat(securityWebFilterChain).isNotNull();
    }

    @Test
    void shouldHaveJwtAuthenticationWebFilterBean() {
        assertThat(jwtAuthenticationWebFilter).isNotNull();
    }

    @Test
    void passwordEncoderShouldBeBCrypt() {
        String password = "testPassword123";
        String encoded = passwordEncoder.encode(password);

        assertThat(encoded).isNotNull();
        assertThat(encoded).startsWith("$2a$"); // BCrypt prefix
        assertThat(passwordEncoder.matches(password, encoded)).isTrue();
    }
}
