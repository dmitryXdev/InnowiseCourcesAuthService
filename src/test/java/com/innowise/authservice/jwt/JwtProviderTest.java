package com.innowise.authservice.jwt;

import com.innowise.authservice.model.Role;
import com.innowise.authservice.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtProviderTest {
    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private JwtDecoder jwtDecoder;

    @InjectMocks
    private JwtProvider provider;

    private User getUser(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);

        return user;
    }

    @Test
    void generateAccessToken_shouldGenerateAccessToken() {
        User user = getUser(0L, Role.ADMIN);

        Jwt jwt = mock(Jwt.class);

        when(jwtEncoder.encode(any())).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn("access-token");

        String token = provider.generateAccessToken(user);

        assertEquals("access-token", token);
    }

    @Test
    void generateRefreshToken_shouldGenerateAndReturnRefreshToken() {
        User user = getUser(0L, Role.ADMIN);

        Jwt jwt = mock(Jwt.class);

        when(jwtEncoder.encode(any())).thenReturn(jwt);
        when(jwt.getTokenValue()).thenReturn("refresh-token");

        String token = provider.generateRefreshToken(user);

        assertEquals("refresh-token", token);
    }

    @Test
    void validateToken_shouldReturnTrueIfTokenIsValid() {
        Jwt jwt = mock(Jwt.class);

        when(jwtDecoder.decode(any())).thenReturn(jwt);
        when(jwt.getExpiresAt()).thenReturn(Instant.now().plusSeconds(15));

        assertTrue(provider.validateToken("valid token"));
    }

    @Test
    void validateToken_shouldReturnFalseIfTokenIsNotValid() {
        Jwt jwt = mock(Jwt.class);

        when(jwtDecoder.decode(any())).thenReturn(jwt);
        when(jwt.getExpiresAt()).thenReturn(Instant.now());

        assertFalse(provider.validateToken("non valid token"));
    }
}
