package com.innowise.authservice.security;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPrincipal {
    private Long id;
    private String role;
}
