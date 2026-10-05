package com.example.petshotel.dto.response;

import java.math.BigDecimal;

import com.example.petshotel.domain.enums.RoomStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ข้อมูลห้องพัก")
public record RoomResponse(
        @Schema(description = "รหัสห้อง", example = "1") Long id,
        @Schema(description = "หมายเลขห้อง", example = "101") String roomNumber,
        @Schema(description = "ชื่อหรือประเภทห้อง", example = "Deluxe") String name,
        @Schema(description = "รายละเอียดห้อง", example = "ห้องกว้าง มีแอร์") String description,
        @Schema(description = "จำนวนสัตว์สูงสุดที่รองรับ", example = "3") Integer capacity,
        @Schema(description = "ราคาต่อสัตว์ 1 ตัวต่อคืน (บาท)", example = "500.00") BigDecimal pricePerPetPerNight,
        @Schema(description = "สถานะห้อง", example = "ACTIVE") RoomStatus status,
        @Schema(description = "path รูปห้อง (ว่างได้)", example = "/uploads/rooms/3f2a.jpg", nullable = true) String imageUrl) {
}