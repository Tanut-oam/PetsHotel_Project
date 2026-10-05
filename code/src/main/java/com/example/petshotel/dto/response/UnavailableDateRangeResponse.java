package com.example.petshotel.dto.response;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ช่วงวันที่ห้องไม่ว่าง")
public record UnavailableDateRangeResponse(
        @Schema(description = "วันแรกที่ห้องไม่ว่าง", example = "2026-10-10") LocalDate unavailableFrom,
        @Schema(description = "วันที่ห้องกลับมาว่าง (วันเช็กเอาต์ของการจองนั้น)", example = "2026-10-12") LocalDate availableAgainOn
) {
}