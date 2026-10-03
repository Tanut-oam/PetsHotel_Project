package com.example.petshotel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.petshotel.domain.enums.PromotionType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ข้อมูลโปรโมชันที่ API ตอบกลับ")
public record PromotionResponse(

    @Schema(
        description = "รหัสโปรโมชัน",
        example = "1"
    )
    Long id,

    @Schema(
        description = "ชื่อโปรโมชัน",
        example = "ลดเดือนตุลาคม"
    )
    String name,

    @Schema(
        description = "รายละเอียดโปรโมชัน อาจเป็น null หากไม่ได้กำหนด",
        example = "ส่วนลดสำหรับการเข้าพักเดือนตุลาคม"
    )
    String description,

    @Schema(
        description = "ประเภทส่วนลด: PERCENTAGE หรือ FIXED_AMOUNT",
        example = "PERCENTAGE"
    )
    PromotionType type,

    @Schema(
        description = "มูลค่าส่วนลด หน่วยเป็นเปอร์เซ็นต์หรือบาทตามประเภท",
        example = "10.00"
    )
    BigDecimal value,

    @Schema(
        description = "วันเริ่มต้นโปรโมชัน รูปแบบ yyyy-MM-dd",
        example = "2026-10-01"
    )
    LocalDate startDate,

    @Schema(
        description = "วันสิ้นสุดโปรโมชัน รูปแบบ yyyy-MM-dd",
        example = "2026-10-31"
    )
    LocalDate endDate,

    @Schema(
        description = "จำนวนคืนขั้นต่ำที่เก็บไว้ในข้อมูลโปรโมชัน "
            + "อาจเป็น null หากไม่ได้กำหนด",
        example = "2"
    )
    Integer minimumNights,

    @Schema(
        description = "สถานะเปิดใช้งาน "
            + "ค่า true ไม่ได้หมายความว่าอยู่ในช่วงวันที่ใช้งานเสมอไป",
        example = "true"
    )
    Boolean active

) {}