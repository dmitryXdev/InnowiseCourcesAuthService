package com.innowise.authservice.dto;

import lombok.Data;

@Data
public class AuthResponseDto {
    private String authToken;
    private String refreshToken;
}
