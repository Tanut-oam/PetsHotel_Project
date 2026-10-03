package com.example.petshotel.dto.request;

import com.example.petshotel.domain.enums.PetType;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpdatePetRequest(
    @NotBlank(message = "Pet name is required")
    @Schema(description = "ชื่อสัตว์เลี้ยง", example = "โมจิ")
    String name,


    @NotNull(message = "Pet type is required")
    @Schema(description = "ประเภทสัตว์เลี้ยง: DOG, CAT หรือ OTHER", example = "DOG")
    PetType type,

    @Schema(description = "สายพันธุ์", example = "ชิบะอินุ")
    String breed,

    @PositiveOrZero(message = "Age must be zero or greater")
    @Schema(description = "อายุสัตว์เลี้ยงเป็นปี", example = "3")
    Integer age,

    @Positive(message = "Weight must be greater than zero")
    @Schema(description = "น้ำหนักสัตว์เลี้ยงเป็นกิโลกรัม", example = "10.5")
    Double weight,

    @Schema(description = "เพศ", example = "ผู้")
    String gender,

    @Schema(description = "ข้อมูลสุขภาพหรือโรคประจำตัว", example = "ไม่มีโรคประจำตัว")
    String medicalNote,

    @Schema(description = "คำแนะนำเรื่องอาหาร", example = "ให้อาหารเม็ดวันละ 2 มื้อ")
    String feedingInstruction,

    @Schema(description = "ข้อควรดูแลเพิ่มเติม", example = "กลัวเสียงดัง")
    String specialNote
) {
}