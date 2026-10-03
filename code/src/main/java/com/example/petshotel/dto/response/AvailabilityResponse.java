package com.example.petshotel.dto.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ผลการค้นหาห้องว่าง")
public record AvailabilityResponse(
        @Schema(description = "วันเช็กอินที่ค้นหา", example = "2026-10-10") LocalDate checkIn,
        @Schema(description = "วันเช็กเอาต์ที่ค้นหา", example = "2026-10-12") LocalDate checkOut,
        @Schema(description = "จำนวนสัตว์เลี้ยงที่ค้นหา", example = "2") Integer petCount,
        @Schema(description = "ห้องที่ว่างตลอดช่วงวันที่และรับสัตว์ได้ตามจำนวน") List<RoomResponse> availableRooms) {
}