package com.example.petshotel.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

@Schema(description = "เงื่อนไขค้นหาห้องพัก (ไม่ใส่ = ไม่กรองตามเงื่อนไขนั้น)")
public record RoomSearchRequest(
        @Schema(description = "คำค้นจากชื่อห้องหรือหมายเลขห้อง", example = "deluxe")
        String keyword,

        @Schema(description = "จำนวนสัตว์เลี้ยงขั้นต่ำที่ห้องต้องรับได้", example = "2", minimum = "1")
        @Min(1) Integer minCapacity,

        @Schema(description = "ราคาสูงสุดต่อตัวต่อคืน (บาท)", example = "800")
        @DecimalMin("0") BigDecimal maxPrice) {
}