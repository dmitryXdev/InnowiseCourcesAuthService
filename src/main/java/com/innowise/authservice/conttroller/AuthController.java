package com.innowise.authservice.conttroller;

import com.innowise.authservice.dto.AuthDto;
import com.innowise.authservice.dto.AuthResponseDto;
import com.innowise.authservice.dto.CreateUserDto;
import com.innowise.authservice.dto.TokenValidationRequestDto;
import com.innowise.authservice.dto.TokenValidationResponseDto;
import com.innowise.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh-token";

    /**
     * Accepts user data to register new user.
     *
     * @param createUserDto
     * @return ResponseEntity<AuthResponseDto>, contains access and refresh tokens
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerNewUser(@RequestBody @Valid CreateUserDto createUserDto) {
        AuthResponseDto authResponseDto = authService.saveUser(createUserDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(authResponseDto);
    }

    /**
     * Authenticates user.
     *
     * @param authDto
     * @return ResponseEntity<AuthResponseDto>, contains access and refresh tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody @Valid AuthDto authDto) {
        AuthResponseDto authResponseDto = authService.loginUser(authDto);

        return ResponseEntity.ok().body(authResponseDto);
    }

    /**
     * Validates token.
     *
     * @param dto
     * @return ResponseEntity<TokenValidationResponseDto>
     */
    @PostMapping("/validate")
    @PreAuthorize("hasRole('ADMIN') || authentication.principal == 'internal-service'")
    public ResponseEntity<TokenValidationResponseDto> validateToken(@RequestBody @Valid TokenValidationRequestDto dto) {
        return ResponseEntity.ok(authService.validateToken(dto));
    }

    /**
     * Returns new access token.
     *
     * @param refreshToken
     * @return ResponseEntity<String>, raw access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@CookieValue(REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {
        String newAccessToken = authService.refreshToken(refreshToken);

        return ResponseEntity.ok().body(newAccessToken);
    }

}
