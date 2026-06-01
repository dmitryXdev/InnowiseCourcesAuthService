package com.innowise.authservice.exception.handler;

import com.innowise.authservice.exception.BadIncomingDataException;
import com.innowise.authservice.exception.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> internalServerError(Exception e) {
        return ResponseEntity.internalServerError().body(
                ErrorResponse.builder()
                        .message(e.getMessage())
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .build()
        );
    }

    @ExceptionHandler(BadIncomingDataException.class)
    public ResponseEntity<ErrorResponse> badIncomingData(BadIncomingDataException e) {
        return ResponseEntity.internalServerError().body(
                ErrorResponse.builder()
                        .message(e.getMessage())
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build()
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> entityNotFound(EntityNotFoundException e) {
        return ResponseEntity.internalServerError().body(
                ErrorResponse.builder()
                        .message(e.getMessage())
                        .statusCode(HttpStatus.BAD_REQUEST.value())
                        .build()
        );
    }
}
