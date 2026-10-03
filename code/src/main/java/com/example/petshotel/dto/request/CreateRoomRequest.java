package com.example.petshotel.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ข้อมูลห้องพัก")
public record CreateRoomRequest(
    @Schema(description = "หมายเลขห้อง (ห้ามซ้ำ)", example = "101")
    @NotBlank(message = "RoomNumber is required")
    String roomNumber,

    @Schema(description = "ชื่อหรือประเภทห้อง", example = "Deluxe")
    @NotBlank(message = "Name is required")
    String name,

    @Schema(description = "รายละเอียดห้อง", example = "ห้องกว้าง มีแอร์และกล้องวงจรปิด")
    String description,

    @Schema(description = "จำนวนสัตว์สูงสุดที่รองรับ", example = "3", minimum = "1")
    @NotNull(message = "Please specify the number of animals accommodated.")
    @Min(value = 1, message = "The room must accommodate at least one animal.")
    Integer capacity,

    @Schema(description = "ราคาต่อสัตว์ 1 ตัวต่อคืน (บาท)", example = "500.00", minimum = "0")
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must not be negative")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 digits and 2 decimal places")
    BigDecimal pricePerPetPerNight
) {}