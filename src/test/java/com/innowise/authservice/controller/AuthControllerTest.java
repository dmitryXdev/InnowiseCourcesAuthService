package com.innowise.authservice.controller;

import com.innowise.authservice.config.TestConfig;
import com.innowise.authservice.dao.RefreshTokenRepository;
import com.innowise.authservice.dao.UserRepository;
import com.innowise.authservice.dto.AuthDto;
import com.innowise.authservice.dto.AuthResponseDto;
import com.innowise.authservice.dto.CreateUserDto;
import com.innowise.authservice.dto.TokenValidationRequestDto;
import com.innowise.authservice.dto.TokenValidationResponseDto;
import com.innowise.authservice.model.Role;
import com.innowise.authservice.service.impl.AuthServiceImpl;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@Import(TestConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AuthServiceImpl authService;

    @Value("${internal.secret.header}")
    private String internalSecretHeader;

    @Value("${internal.secret}")
    private String secret;

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh-token";

    @AfterEach
    void clearDB() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    private CreateUserDto getRegisterUserDto(Long id, Role role) {
        CreateUserDto register = new CreateUserDto();
        register.setLogin("Login");
        register.setPassword("12345password");
        register.setEmail("some@email.com");
        register.setRole(role.name());
        register.setId(id);

        return register;
    }

    @Test
    void registerNewUser_shouldSaveNewUserAndReturnTokensInCookies() throws Exception {
        CreateUserDto register = getRegisterUserDto(0L, Role.ADMIN);

        AuthResponseDto responseDto = objectMapper.readValue(mockMvc.perform(post("/auth/register")
                        .header(internalSecretHeader, secret)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), AuthResponseDto.class);

        assertNotNull(responseDto);
        assertFalse(responseDto.getRefreshToken().isEmpty());
        assertFalse(responseDto.getAuthToken().isEmpty());
    }

    @Test
    void login_shouldLoginUserAndReturnTokensInCookies() throws Exception {
        CreateUserDto register = getRegisterUserDto(0L, Role.USER);

        authService.saveUser(register);

        AuthDto authDto = new AuthDto();
        authDto.setPassword(register.getPassword());
        authDto.setLogin(register.getLogin());

        AuthResponseDto responseDto = objectMapper.readValue(mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(authDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), AuthResponseDto.class);

        assertNotNull(responseDto);
        assertFalse(responseDto.getAuthToken().isEmpty());
        assertFalse(responseDto.getRefreshToken().isEmpty());
    }

    @Test
    void validateToken_shouldValidateToken() throws Exception {
        CreateUserDto register = getRegisterUserDto(0L, Role.ADMIN);

        AuthResponseDto responseDto = objectMapper.readValue(mockMvc.perform(post("/auth/register")
                        .header(internalSecretHeader, secret)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString(), AuthResponseDto.class);

        TokenValidationRequestDto requestDto = new TokenValidationRequestDto();
        requestDto.setToken(responseDto.getAuthToken());

        TokenValidationResponseDto tokenResponseDto = objectMapper.readValue(mockMvc.perform(post("/auth/validate")
                        .header(internalSecretHeader, secret)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(), TokenValidationResponseDto.class);

        assertNotNull(tokenResponseDto);
        assertEquals(register.getId(), tokenResponseDto.getUserId());
    }

    @Test
    void refreshToken_shouldGetNewAccessToken() throws Exception {
        CreateUserDto register = getRegisterUserDto(0L, Role.USER);

        AuthResponseDto auth = authService.saveUser(register);

        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, auth.getRefreshToken());

        String response = mockMvc.perform(post("/auth/refresh")
                        .header(internalSecretHeader, secret)
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertNotNull(response);
        assertFalse(response.isEmpty());
    }
}
