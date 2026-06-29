package com.innowise.authservice.service.impl;

import com.innowise.authservice.dao.UserRepository;
import com.innowise.authservice.dto.AuthDto;
import com.innowise.authservice.dto.AuthResponseDto;
import com.innowise.authservice.dto.CreateUserDto;
import com.innowise.authservice.dto.TokenValidationRequestDto;
import com.innowise.authservice.dto.TokenValidationResponseDto;
import com.innowise.authservice.exception.BadIncomingDataException;
import com.innowise.authservice.exception.EntityNotFoundException;
import com.innowise.authservice.jwt.JwtProvider;
import com.innowise.authservice.model.RefreshToken;
import com.innowise.authservice.model.Role;
import com.innowise.authservice.model.User;
import com.innowise.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    private static final String USER_NOT_FOUND_MESSAGE = "User not found";

    /**
     * Saves user, generates and returns access tokens.
     *
     * @param dto - CreateUserDto
     * @return AuthResponseDto
     * @throws BadIncomingDataException - incoming data is incorrect.
     */
    @Override
    @Transactional
    public AuthResponseDto saveUser(CreateUserDto dto) {
        if (dto == null) {
            throw new BadIncomingDataException("Bad incoming data");
        }

        User user = userRepository.findById(dto.getId()).orElse(null);

        if (user != null) {
            throw new BadIncomingDataException("User already exists");
        }

        user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setLogin(dto.getLogin());
        user.setRole(Role.valueOf(dto.getRole()));
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        userRepository.save(user);

        String refreshToken = jwtProvider.generateRefreshToken(user);

        RefreshToken tokenEntity = new RefreshToken();
        tokenEntity.setUser(user);
        tokenEntity.setToken(refreshToken);
        tokenEntity.setExpiresAt(jwtProvider.getTokenExpiration(refreshToken));

        user.setToken(tokenEntity);

        AuthResponseDto response = new AuthResponseDto();
        response.setAuthToken(jwtProvider.generateAccessToken(user));
        response.setRefreshToken(refreshToken);

        return response;
    }

    /**
     * Validates user credentials and returns access tokens.
     *
     * @param authDto - AuthDto
     * @return AuthResponseDto
     * @throws BadIncomingDataException - if incoming data is incorrect
     */
    @Override
    @Transactional
    public AuthResponseDto loginUser(AuthDto authDto) {
        if (authDto == null) {
            throw new BadIncomingDataException("Bad incoming data");
        }

        User user = userRepository.findByLogin(authDto.getLogin()).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));

        if (!passwordEncoder.matches(authDto.getPassword(), user.getPassword())) {
            throw new BadIncomingDataException("Invalid credentials");
        }

        String newRefreshToken = jwtProvider.generateRefreshToken(user);

        RefreshToken token = user.getToken();
        if (token == null) {
            token = new RefreshToken();
        }

        token.setToken(newRefreshToken);
        token.setExpiresAt(jwtProvider.getTokenExpiration(newRefreshToken));

        AuthResponseDto authResponseDto = new AuthResponseDto();
        authResponseDto.setAuthToken(jwtProvider.generateAccessToken(user));
        authResponseDto.setRefreshToken(newRefreshToken);

        return authResponseDto;
    }

    /**
     * Validates token. Used by internal services.
     *
     * @param dto
     * @return TokenValidationResponse
     */
    @Override
    public TokenValidationResponseDto validateToken(TokenValidationRequestDto dto) {
        if (!jwtProvider.validateToken(dto.getToken())) {
            return TokenValidationResponseDto.builder()
                    .valid(false)
                    .role(null)
                    .userId(null)
                    .build();
        }

        Jwt jwt = jwtProvider.getJwtInstanceFromString(dto.getToken());

        return TokenValidationResponseDto.builder()
                .valid(true)
                .userId(Long.parseLong(jwt.getSubject()))
                .role(jwt.getClaim("role"))
                .build();
    }

    /**
     * Refreshes access token.
     *
     * @param refreshToken
     * @return String - raw access token
     * @throws BadIncomingDataException - if refresh token is invalid.
     * @throws EntityNotFoundException - if user that linked to the token is not found.
     */
    @Override
    public String refreshToken(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BadIncomingDataException("Bad token");
        }

        Jwt jwt = jwtProvider.getJwtInstanceFromString(refreshToken);
        User user = userRepository.findById(Long.valueOf(jwt.getSubject()))
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));

        return jwtProvider.generateAccessToken(user);
    }
}
