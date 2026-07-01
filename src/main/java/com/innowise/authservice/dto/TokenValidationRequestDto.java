package com.innowise.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TokenValidationRequestDto {
    @NotNull
    @NotBlank
    private String token;
}
