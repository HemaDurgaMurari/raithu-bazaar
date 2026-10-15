package com.klu.raithubazaar.security;

import com.klu.raithubazaar.model.Role;
import com.klu.raithubazaar.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    @Test
    void generatedTokenAuthenticatesTheMatchingAccount() {
        JwtService service = new JwtService(
                "a-long-local-test-secret-with-more-than-thirty-two-characters", 60_000);
        User account = new User("Asha", "asha@example.com", "encoded-password", Role.CUSTOMER);
        UserDetails details = org.springframework.security.core.userdetails.User
                .withUsername("asha@example.com")
                .password("encoded-password")
                .authorities("ROLE_CUSTOMER")
                .build();

        String token = service.generateToken(account);

        assertThat(service.extractEmail(token)).isEqualTo("asha@example.com");
        assertThat(service.isTokenValid(token, details)).isTrue();
    }
}
