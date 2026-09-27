package com.example.petshotel.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;

public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors) {

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return of(status, message, path, Map.of());
    }

    public static ErrorResponse of(HttpStatus status, String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path, fieldErrors);
    }
}