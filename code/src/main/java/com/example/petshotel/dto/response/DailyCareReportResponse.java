package com.example.petshotel.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;
public record DailyCareReportResponse(
    @Schema(description = "รหัสรายงาน", example = "25")
    Long id,

    @Schema(description = "รหัสสัตว์เลี้ยงในการจอง", example = "12")
    Long bookingPetId,

    @Schema(description = "รหัสการจอง", example = "5")
    Long bookingId,

    @Schema(description = "รหัสสัตว์เลี้ยง", example = "3")
    Long petId,

    @Schema(description = "ชื่อสัตว์เลี้ยง", example = "โมจิ")
    String petName,

    @Schema(description = "รหัสผู้บันทึกรายงาน", example = "2")
    Long recordedById,

    @Schema(description = "วันที่รายงาน", example = "2026-10-03")
    LocalDate reportDate,

    @Schema(description = "อาหารที่ให้ช่วงเช้า", example = "อาหารเม็ด 100 กรัม")
    String feedingMorning,

    @Schema(description = "อาหารที่ให้ช่วงเย็น", example = "อาหารเปียก 1 ซอง")
    String feedingEvening,

    @Schema(description = "ระยะเวลาเดินเล่นเป็นนาที", example = "15")
    Integer walkingMinutes,

    @Schema(description = "การดูแลความสะอาด", example = "แปรงขน")
    String grooming,

    @Schema(description = "อารมณ์ของสัตว์เลี้ยง", example = "ร่าเริง")
    String mood,

    @Schema(description = "ข้อมูลสุขภาพประจำวัน", example = "สุขภาพปกติ")
    String healthNote,

    @Schema(description = "หมายเหตุเพิ่มเติม", example = "กินอาหารครบทั้งสองมื้อ")
    String generalNote,

    @Schema(description = "เวลาที่สร้างรายงาน", example = "2026-10-03T17:30:00")
    LocalDateTime createdAt
) {}