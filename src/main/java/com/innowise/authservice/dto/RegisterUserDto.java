package com.innowise.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterUserDto {
    @NotNull
    private Long id;
    @NotNull
    @NotBlank
    @Email
    private String email;
    @NotNull
    @NotBlank
    private String login;
    @NotNull
    @NotBlank
    @Pattern(regexp = "^[A-Za-z0-9\\[\\]\\-\\/\"~!@#$%^&*_+=`|(){}:;'<>,.?]{6,}$")
    private String password;
    @NotNull
    private String role;
}
