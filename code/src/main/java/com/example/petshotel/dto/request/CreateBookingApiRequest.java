package com.example.petshotel.dto.request;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ข้อมูลสำหรับสร้างการจองหรือคำนวณราคา")
public record CreateBookingApiRequest(

        @Schema(description = "รหัสห้อง", example = "1")
        @NotNull(message = "Room ID is required")
        Long roomId,

        @Schema(description = "รหัสสัตว์ที่จะเข้าพัก (ต้องเป็นของผู้ใช้ที่ล็อกอิน)", example = "[3, 4]")
        @NotEmpty(message = "At least one pet is required")
        List<@NotNull(message = "Pet ID must not be null") Long> petIds,

        @Schema(description = "วันเช็กอิน", example = "2026-11-01")
        @NotNull(message = "Check-in date is required")
        LocalDate checkInDate,

        @Schema(description = "วันเช็กเอาต์ (ต้องหลังวันเช็กอิน)", example = "2026-11-03")
        @NotNull(message = "Check-out date is required")
        LocalDate checkOutDate,

        @Schema(description = "บริการเสริม: key = รหัสบริการ, value = รหัสสัตว์ที่รับบริการ",
                example = "{\"2\": [3]}", nullable = true)
        Map<Long, List<Long>> servicePetIds,

        @Schema(description = "รหัสโปรโมชัน (ไม่บังคับ)", example = "1", nullable = true)
        Long promotionId
) {
}
