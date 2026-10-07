package com.example.petshotel.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.petshotel.domain.enums.PromotionType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "ข้อมูลสำหรับเพิ่มหรือแก้ไขโปรโมชัน")
public record CreatePromotionRequest(

    @Schema(
        description = "ชื่อโปรโมชัน",
        example = "ลดเดือนตุลาคม"
    )
    @NotBlank(message = "Promotion name is required")
    String name,

    @Schema(
        description = "ประเภทส่วนลด: PERCENTAGE คือเปอร์เซ็นต์ "
            + "และ FIXED_AMOUNT คือจำนวนเงินเป็นบาท",
        example = "PERCENTAGE"
    )
    @NotNull(message = "Promotion type is required")
    PromotionType type,

    @Schema(
        description = "มูลค่าส่วนลด ต้องมากกว่า 0 "
            + "ถ้าเป็นเปอร์เซ็นต์ต้องไม่เกิน 100 "
            + "รองรับทศนิยมไม่เกิน 2 ตำแหน่ง",
        example = "10.00"
    )
    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be greater than zero")
    @Digits(
        integer = 8,
        fraction = 2,
        message = "Discount value must have at most 8 digits and 2 decimal places"
    )
    BigDecimal discountValue,

    @Schema(
        description = "วันเริ่มต้นโปรโมชัน รูปแบบ yyyy-MM-dd",
        example = "2026-10-01"
    )
    @NotNull(message = "Start date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate startDate,

    @Schema(
        description = "วันสิ้นสุดโปรโมชัน รูปแบบ yyyy-MM-dd "
            + "ต้องไม่ก่อนวันเริ่มต้น และเป็นวันเดียวกันได้",
        example = "2026-10-31"
    )
    @NotNull(message = "End date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate endDate,

    @Schema(
        description = "สถานะเปิดใช้งาน: true คือเปิดใช้ "
            + "โปรโมชันจะใช้งานได้เมื่ออยู่ในช่วงวันที่กำหนดด้วย",
        example = "true"
    )
    boolean active,

    @Schema(
        description = "รายละเอียดโปรโมชัน ไม่บังคับกรอก "
            + "เว้นว่างหรือส่ง null หมายถึงไม่กำหนดรายละเอียด",
        example = "ส่วนลดสำหรับการเข้าพักอย่างน้อย 3 คืน"
    )
    String description,

    @Schema(
        description = "จำนวนคืนขั้นต่ำ ไม่บังคับกรอก "
            + "หากระบุต้องเป็นจำนวนเต็มตั้งแต่ 1 "
            + "null หมายถึงไม่กำหนดขั้นต่ำ",
        example = "3"
    )
    @Positive(message = "Minimum nights must be at least 1")
    Integer minimumNights

) {}