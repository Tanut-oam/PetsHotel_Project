package com.example.petshotel.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "ข้อมูลสำหรับเพิ่มหรือแก้ไขบริการเสริม")
public record ExtraServiceRequest(

    @Schema(
        description = "ชื่อบริการเสริม",
        example = "อาบน้ำ"
    )
    @NotBlank(message = "Extra service name is required")
    String name,

    @Schema(
        description = "รายละเอียดบริการเสริม ไม่จำเป็นต้องระบุ "
            + "หากไม่ส่งตอนแก้ไข รายละเอียดเดิมจะถูกเปลี่ยนเป็น null",
        example = "บริการอาบน้ำและเป่าขนสัตว์เลี้ยง"
    )
    String description,

    @Schema(
        description = "ราคาบริการเสริม หน่วยเป็นบาท "
            + "ต้องไม่ติดลบ รองรับทศนิยมไม่เกิน 2 ตำแหน่ง "
            + "และจำนวนเต็มไม่เกิน 8 หลัก",
        example = "250.00"
    )
    @NotNull(message = "Price is required")
    @DecimalMin(
        value = "0.00",
        message = "Price must not be negative"
    )
    @Digits(
        integer = 8,
        fraction = 2,
        message = "Price must have at most 8 digits and 2 decimal places"
    )
    BigDecimal price,

    @Schema(
        description = "สถานะเปิดใช้งาน: true คือเปิดใช้ false คือปิดใช้ "
            + "หากไม่ส่งหรือส่งเป็น null ตอนเพิ่มจะเปิดใช้ "
            + "แต่ตอนแก้ไขจะคงสถานะเดิม",
        example = "true"
    )
    Boolean active

) {}