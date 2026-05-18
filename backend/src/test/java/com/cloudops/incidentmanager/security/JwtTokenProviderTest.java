package com.cloudops.incidentmanager.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-min-32-chars-long!!";
    private static final long EXPIRATION_MS = 3_600_000L;
    private static final long REFRESH_EXPIRATION_MS = 604_800_000L;

    private JwtTokenProvider jwtTokenProvider;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(SECRET, EXPIRATION_MS, REFRESH_EXPIRATION_MS);
        jwtTokenProvider = new JwtTokenProvider(props);
        userDetails = new User(
                "admin@example.com",
                "hashed-password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Test
    void generateAccessToken_returnsNonBlankToken() {
        String token = jwtTokenProvider.generateAccessToken(userDetails);
        assertThat(token).isNotBlank();
    }

    @Test
    void generateRefreshToken_returnsNonBlankToken() {
        String token = jwtTokenProvider.generateRefreshToken(userDetails);
        assertThat(token).isNotBlank();
    }

    @Test
    void extractUsername_returnsCorrectEmail() {
        String token = jwtTokenProvider.generateAccessToken(userDetails);
        assertThat(jwtTokenProvider.extractUsername(token)).isEqualTo("admin@example.com");
    }

    @Test
    void isTokenValid_withValidToken_returnsTrue() {
        String token = jwtTokenProvider.generateAccessToken(userDetails);
        assertThat(jwtTokenProvider.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_withWrongUser_returnsFalse() {
        String token = jwtTokenProvider.generateAccessToken(userDetails);
        UserDetails other = new User("other@example.com", "pw", List.of());
        assertThat(jwtTokenProvider.isTokenValid(token, other)).isFalse();
    }

    @Test
    void isTokenValid_withTamperedToken_returnsFalse() {
        String token = jwtTokenProvider.generateAccessToken(userDetails);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(jwtTokenProvider.isTokenValid(tampered, userDetails)).isFalse();
    }

    @Test
    void isTokenValid_withExpiredToken_returnsFalse() {
        JwtProperties shortProps = new JwtProperties(SECRET, -1L, REFRESH_EXPIRATION_MS);
        JwtTokenProvider shortProvider = new JwtTokenProvider(shortProps);
        String token = shortProvider.generateAccessToken(userDetails);
        assertThat(shortProvider.isTokenValid(token, userDetails)).isFalse();
    }

    @Test
    void accessToken_and_refreshToken_areDifferent() {
        String access = jwtTokenProvider.generateAccessToken(userDetails);
        String refresh = jwtTokenProvider.generateRefreshToken(userDetails);
        assertThat(access).isNotEqualTo(refresh);
    }
}
