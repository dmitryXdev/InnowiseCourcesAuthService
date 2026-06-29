package com.innowise.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserDto {
    @NotNull
    private Long id;
    @NotNull
    @NotBlank
    private String email;
    @NotNull
    @NotBlank
    private String role;
}
