package com.innowise.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserDto {
    @Email
    private String email;
    @NotBlank
    private String password;
}
