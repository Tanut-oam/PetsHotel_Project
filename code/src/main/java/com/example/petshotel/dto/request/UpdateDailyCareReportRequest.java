package com.example.petshotel.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateDailyCareReportRequest(
    @NotNull(message = "Booking pet ID is required")
    @Positive(message = "Booking pet ID must be greater than zero")
    @Schema(description = "รหัสสัตว์เลี้ยงในการจอง ต้องอยู่ในการจองเดิม", example = "12")
    Long bookingPetId,

    @NotNull(message = "Report date is required")
    @Schema(description = "วันที่รายงาน ต้องอยู่ในช่วงเข้าพัก", example = "2026-10-03")
    LocalDate reportDate,

    @Schema(description = "อาหารที่ให้ช่วงเช้า", example = "อาหารเม็ด 100 กรัม")
    String feedingMorning,

    @Schema(description = "อาหารที่ให้ช่วงเย็น", example = "อาหารเปียก 1 ซอง")
    String feedingEvening,

    @PositiveOrZero(message = "Walking minutes must be zero or greater")
    @Schema(description = "ระยะเวลาเดินเล่นเป็นนาที", example = "15")
    Integer walkingMinutes,

    @Schema(description = "การดูแลความสะอาด", example = "แปรงขน")
    String grooming,

    @Schema(description = "อารมณ์ของสัตว์เลี้ยง", example = "ร่าเริง")
    String mood,

    @Schema(description = "ข้อมูลสุขภาพประจำวัน", example = "สุขภาพปกติ")
    String healthNote,

    @Schema(description = "หมายเหตุเพิ่มเติม", example = "กินอาหารครบทั้งสองมื้อ")
    String generalNote
) {
    
}
