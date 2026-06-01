package com.innowise.authservice.controller;

import com.innowise.authservice.config.TestConfig;
import com.innowise.authservice.dao.RefreshTokenRepository;
import com.innowise.authservice.dao.UserRepository;
import com.innowise.authservice.dto.AuthDto;
import com.innowise.authservice.dto.AuthResponseDto;
import com.innowise.authservice.dto.RegisterUserDto;
import com.innowise.authservice.dto.TokenValidationRequestDto;
import com.innowise.authservice.dto.TokenValidationResponseDto;
import com.innowise.authservice.model.Role;
import com.innowise.authservice.service.impl.AuthServiceImpl;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
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
@ExtendWith(MockitoExtension.class)
@Testcontainers
@Import(TestConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {
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

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access-token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh-token";

    @AfterEach
    void clearDB() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    private RegisterUserDto getRegisterUserDto(Long id, Role role) {
        RegisterUserDto register = new RegisterUserDto();
        register.setLogin("Login");
        register.setPassword("12345password");
        register.setEmail("some@email.com");
        register.setRole(role.name());
        register.setId(id);

        return register;
    }

    @Test
    void registerNewUser_shouldSaveNewUserAndReturnTokensInCookies() throws Exception {
        RegisterUserDto register = getRegisterUserDto(0L, Role.ADMIN);

        MockHttpServletResponse response = mockMvc.perform(post("/auth/register")
                        .header(internalSecretHeader, secret)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        Cookie access = response.getCookie(ACCESS_TOKEN_COOKIE_NAME);
        Cookie refresh = response.getCookie(REFRESH_TOKEN_COOKIE_NAME);

        assertNotNull(access);
        assertNotNull(refresh);
        assertFalse(access.getValue().isEmpty());
        assertFalse(refresh.getValue().isEmpty());
    }
    
    @Test
    void login_shouldLoginUserAndReturnTokensInCookies() throws Exception {
        RegisterUserDto register = getRegisterUserDto(0L, Role.USER);

        authService.saveUser(register);

        AuthDto authDto = new AuthDto();
        authDto.setPassword(register.getPassword());
        authDto.setLogin(register.getLogin());

        MockHttpServletResponse response = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(authDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        Cookie access = response.getCookie(ACCESS_TOKEN_COOKIE_NAME);
        Cookie refresh = response.getCookie(REFRESH_TOKEN_COOKIE_NAME);

        assertNotNull(access);
        assertNotNull(refresh);
        assertFalse(access.getValue().isEmpty());
        assertFalse(refresh.getValue().isEmpty());
    }

    @Test
    void validateToken_shouldValidateToken() throws Exception {
        RegisterUserDto register = getRegisterUserDto(0L, Role.ADMIN);

        MockHttpServletResponse response = mockMvc.perform(post("/auth/register")
                        .header(internalSecretHeader, secret)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        Cookie access = response.getCookie(ACCESS_TOKEN_COOKIE_NAME);

        TokenValidationRequestDto requestDto = new TokenValidationRequestDto();
        requestDto.setToken(access.getValue());

        response = mockMvc.perform(post("/auth/validate")
                .header(internalSecretHeader, secret)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        TokenValidationResponseDto responseDto = objectMapper
                .readValue(response.getContentAsString(), TokenValidationResponseDto.class);

        assertNotNull(responseDto);
        assertEquals(register.getId(), responseDto.getUserId());
    }

    @Test
    void refreshToken_shouldGetNewAccessToken() throws Exception {
        RegisterUserDto register = getRegisterUserDto(0L, Role.USER);

        AuthResponseDto auth = authService.saveUser(register);

        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, auth.getRefreshToken());

        MockHttpServletResponse response = mockMvc.perform(post("/auth/refresh")
                .header(internalSecretHeader, secret)
                .cookie(cookie))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        cookie = response.getCookie(ACCESS_TOKEN_COOKIE_NAME);

        assertNotNull(cookie);
        assertNotNull(cookie.getValue());
        assertFalse(cookie.getValue().isEmpty());
    }
}
