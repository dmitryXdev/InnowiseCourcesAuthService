package com.innowise.authservice.service;

import com.innowise.authservice.dao.UserRepository;
import com.innowise.authservice.dto.AuthDto;
import com.innowise.authservice.dto.AuthResponseDto;
import com.innowise.authservice.dto.RegisterUserDto;
import com.innowise.authservice.dto.TokenValidationRequestDto;
import com.innowise.authservice.dto.TokenValidationResponseDto;
import com.innowise.authservice.jwt.JwtProvider;
import com.innowise.authservice.model.RefreshToken;
import com.innowise.authservice.model.Role;
import com.innowise.authservice.model.User;
import com.innowise.authservice.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private JwtProvider jwtProvider;

    private AuthServiceImpl authService;

    @BeforeEach
    public void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtProvider);
    }

    private void setUpJwtProvider(String accessToken, String refreshToken) {
        when(jwtProvider.generateAccessToken(any(User.class))).thenReturn(accessToken);
        when(jwtProvider.generateRefreshToken(any(User.class))).thenReturn(refreshToken);
        when(jwtProvider.getTokenExpiration(any(String.class))).thenReturn(Instant.now().plusSeconds(15));
    }

    @Test
    void saveUser_shouldSaveUserAndReturnJwtTokens() {
        RegisterUserDto dto = new RegisterUserDto();
        dto.setId(0L);
        dto.setEmail("some@email.com");
        dto.setPassword("asldks2193i");
        dto.setRole("ADMIN");
        dto.setLogin("login");

        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.empty());
        setUpJwtProvider(accessToken, refreshToken);

        AuthResponseDto response = authService.saveUser(dto);

        assertNotNull(response);
        assertNotNull(response.getAuthToken());
        assertNotNull(response.getRefreshToken());

        assertEquals(accessToken, response.getAuthToken());
        assertEquals(refreshToken, response.getRefreshToken());
    }

    @Test
    void loginUser_shouldReturnAuthorizationTokensByUserCredentials() {
        AuthDto authDto = new AuthDto();
        authDto.setLogin("login");
        authDto.setPassword("aksdjlk231");

        User user = new User();
        user.setId(0L);
        user.setLogin(authDto.getLogin());
        user.setPassword(passwordEncoder.encode(authDto.getPassword()));

        RefreshToken token = new RefreshToken();
        token.setToken("asda");
        token.setUser(user);

        user.setToken(token);

        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        when(userRepository.findByLogin(any(String.class))).thenReturn(Optional.of(user));
        setUpJwtProvider(accessToken, refreshToken);

        AuthResponseDto dto = authService.loginUser(authDto);

        assertNotNull(dto);
        assertEquals(accessToken, dto.getAuthToken());
        assertEquals(refreshToken, dto.getRefreshToken());
    }

    @Test
    void validateToken_shouldValidateTokenAndReturnItsData() {
        TokenValidationRequestDto dto = new TokenValidationRequestDto();
        dto.setToken("valid-token");

        Jwt jwt = mock(Jwt.class);

        when(jwtProvider.validateToken(any(String.class))).thenReturn(true);
        when(jwtProvider.getJwtInstanceFromString(any(String.class))).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn("0");
        when(jwt.getClaim("role")).thenReturn(Role.ADMIN.toString());

        TokenValidationResponseDto response = authService.validateToken(dto);

        assertNotNull(response);
        assertTrue(response.isValid());
        assertEquals(Role.ADMIN.toString(), response.getRole());
        assertEquals(0L, response.getUserId());
    }

    @Test
    void refreshToken_shouldGetNewAccessToken() {

    }
}
