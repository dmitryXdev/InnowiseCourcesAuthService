package com.innowise.authservice.exception.handler;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ErrorResponse {
    private String message;
    private Integer statusCode;
}
