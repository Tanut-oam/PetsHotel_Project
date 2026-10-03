package com.example.petshotel.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "เงื่อนไขค้นหาห้องว่าง")
public record AvailabilitySearchRequest(
        @Schema(description = "วันเช็กอิน (yyyy-MM-dd)", example = "2026-10-10")
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,

        @Schema(description = "วันเช็กเอาต์ ต้องอยู่หลังวันเช็กอิน (yyyy-MM-dd)", example = "2026-10-12")
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,

        @Schema(description = "จำนวนสัตว์เลี้ยงที่จะเข้าพัก", example = "2", minimum = "1")
        @NotNull @Min(1) Integer petCount) {
}