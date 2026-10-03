package com.example.petshotel.dto.response;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ช่วงวันที่ห้องไม่ว่างภายในช่วงปฏิทินที่ขอ")
public record RoomAvailabilityCalendarResponse(
        @Schema(description = "รหัสห้อง", example = "1") Long roomId,
        @Schema(description = "วันเริ่มต้นของปฏิทิน", example = "2026-10-01") LocalDate fromDate,
        @Schema(description = "วันสิ้นสุดของปฏิทิน", example = "2026-10-31") LocalDate toDate,
        @Schema(description = "ช่วงวันที่ที่ห้องถูกจองแล้ว") List<UnavailableDateRangeResponse> unavailableRanges
) {
}