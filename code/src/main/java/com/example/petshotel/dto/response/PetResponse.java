package com.example.petshotel.dto.response;

import com.example.petshotel.domain.enums.PetType;
import io.swagger.v3.oas.annotations.media.Schema;

public record PetResponse(
    @Schema(description = "รหัสสัตว์เลี้ยง", example = "1")
    Long id,

    @Schema(description = "ชื่อสัตว์เลี้ยง", example = "โมจิ")
    String name,

    @Schema(description = "ประเภทสัตว์เลี้ยง", example = "DOG")
    PetType type,

    @Schema(description = "สายพันธุ์", example = "ชิบะอินุ")
    String breed,

    @Schema(description = "อายุเป็นปี", example = "3")
    Integer age,

    @Schema(description = "น้ำหนักเป็นกิโลกรัม", example = "10.5")
    Double weight,

    @Schema(description = "เพศ", example = "เพศผู้")
    String gender,

    @Schema(description = "ข้อมูลสุขภาพหรือโรคประจำตัว", example = "ไม่มีโรคประจำตัว")
    String medicalNote,

    @Schema(description = "คำแนะนำเรื่องอาหาร", example = "ให้อาหารเม็ดวันละ 2 มื้อ")
    String feedingInstruction,

    @Schema(description = "ข้อควรดูแลเพิ่มเติม", example = "กลัวเสียงดัง")
    String specialNote,

    @Schema(description = "รหัสเจ้าของ", example = "1")
    Long ownerId,

    @Schema(description = "ชื่อเจ้าของ", example = "สมชาย ใจดี")
    String ownerName,

    @Schema(description = "สถานะใช้งานของสัตว์เลี้ยง", example = "true")
    Boolean active,

    @Schema(description = "ที่อยู่รูปภาพสัตว์เลี้ยง", example = "/uploads/pets/mochi.jpg")
    String imageUrl
) {}