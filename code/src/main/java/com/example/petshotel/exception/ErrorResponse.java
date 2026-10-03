package com.example.petshotel.exception;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "รูปแบบ error มาตรฐานของทุก REST API (สร้างโดย GlobalExceptionHandler)")
public record ErrorResponse(
        @Schema(description = "เวลาที่เกิด error", example = "2026-10-03T10:15:30")
        LocalDateTime timestamp,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "ชื่อ HTTP status", example = "Bad Request")
        String error,

        @Schema(description = "ข้อความอธิบาย error", example = "วันที่ Check-out ต้องอยู่หลังวันที่ Check-in")
        String message,

        @Schema(description = "path ที่เรียก", example = "/api/rooms/available")
        String path,

        @Schema(description = "error รายช่อง (กรณีข้อมูลไม่ผ่าน validation) key = ชื่อ field",
                example = "{\"petCount\": \"must be greater than or equal to 1\"}")
        Map<String, String> fieldErrors) {

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return of(status, message, path, Map.of());
    }

    public static ErrorResponse of(HttpStatus status, String message, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, path, fieldErrors);
    }
}