package com.innowise.authservice.service;

import com.innowise.authservice.dto.AuthDto;
import com.innowise.authservice.dto.AuthResponseDto;
import com.innowise.authservice.dto.RegisterUserDto;
import com.innowise.authservice.dto.TokenValidationRequestDto;
import com.innowise.authservice.dto.TokenValidationResponseDto;

public interface AuthService {
    AuthResponseDto saveUser(RegisterUserDto dto);
    AuthResponseDto loginUser(AuthDto authDto);
    TokenValidationResponseDto validateToken(TokenValidationRequestDto dto);
    String refreshToken(String refreshToken);
}
