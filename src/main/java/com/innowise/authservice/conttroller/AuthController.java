package com.innowise.authservice.conttroller;

import com.innowise.authservice.dto.AuthDto;
import com.innowise.authservice.dto.AuthResponseDto;
import com.innowise.authservice.dto.RegisterUserDto;
import com.innowise.authservice.dto.TokenValidationRequestDto;
import com.innowise.authservice.dto.TokenValidationResponseDto;
import com.innowise.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access-token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh-token";
    private static final String SAME_SITE_COOKIE_ATTRIBUTE = "Strict";

    @PostMapping("/register")
    public ResponseEntity<Void> registerNewUser(@RequestBody RegisterUserDto registerUserDto) {
        AuthResponseDto authResponseDto = authService.saveUser(registerUserDto);

        ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, authResponseDto.getAuthToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .sameSite(SAME_SITE_COOKIE_ATTRIBUTE)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, authResponseDto.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite(SAME_SITE_COOKIE_ATTRIBUTE)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).
                header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody AuthDto authDto) {
        AuthResponseDto authResponseDto = authService.loginUser(authDto);

        ResponseCookie accessCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, authResponseDto.getAuthToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .sameSite(SAME_SITE_COOKIE_ATTRIBUTE)
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, authResponseDto.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(7))
                .sameSite(SAME_SITE_COOKIE_ATTRIBUTE)
                .build();

        return ResponseEntity.ok().
                header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponseDto> validateToken(@RequestBody TokenValidationRequestDto dto) {
        return ResponseEntity.ok(authService.validateToken(dto));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshToken(@CookieValue(REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {
        String newAccessToken = authService.refreshToken(refreshToken);

        ResponseCookie accessToken = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, newAccessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .sameSite(SAME_SITE_COOKIE_ATTRIBUTE)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessToken.toString())
                .build();
    }

}
