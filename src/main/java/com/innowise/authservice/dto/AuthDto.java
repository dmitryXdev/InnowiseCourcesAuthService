package com.innowise.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AuthDto {
    @NotNull
    @NotBlank
    private String login;

    @NotNull
    @NotBlank
    private String password;
}
