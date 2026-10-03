package com.example.petshotel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.petshotel.domain.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ผลการยืนยันรับชำระเงิน")
public record PaymentResponse(
    @Schema(description = "รหัสการจอง", example = "1") Long bookingId,
    @Schema(description = "สถานะการชำระเงิน", example = "PAID") PaymentStatus paymentStatus,
    @Schema(description = "ยอดที่ชำระ (บาท)", example = "1500.00") BigDecimal paidAmount,
    @Schema(description = "เวลาที่ยืนยันรับเงิน", example = "2026-10-01T10:00:00") LocalDateTime paidAt,
    @Schema(description = "รหัสใบเสร็จ", example = "1") Long receiptId,
    @Schema(description = "เลขที่ใบเสร็จ", example = "REC-3f2a9c1e") String receiptNumber
) {
}