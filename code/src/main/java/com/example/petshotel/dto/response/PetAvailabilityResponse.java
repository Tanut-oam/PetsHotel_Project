package com.example.petshotel.dto.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ข้อมูลสัตว์ที่ติดจองในช่วงวันที่ระบุ")
public record PetAvailabilityResponse(
        
        @Schema(description = "วันเช็กอิน", example = "2026-11-01")
        LocalDate checkInDate,
        
        @Schema(description = "วันเช็กเอาต์", example = "2026-11-03")
        LocalDate checkOutDate,
        
        @Schema(description = "รหัสสัตว์ที่ติดจองในช่วงนี้", example = "[3]")
        List<Long> unavailablePetIds
) {
}