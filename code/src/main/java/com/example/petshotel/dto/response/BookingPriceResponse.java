package com.example.petshotel.dto.response;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ราคาที่คำนวณได้ (บาท)")
public record BookingPriceResponse(
        @Schema(description = "ค่าห้อง", example = "1000.00")
        BigDecimal basePrice,

        @Schema(description = "ค่าบริการเสริม", example = "300.00")
        BigDecimal extraServicesPrice,

        @Schema(description = "ค่าบวกวันหยุด", example = "0.00")
        BigDecimal holidaySurcharge,

        @Schema(description = "ส่วนลด", example = "130.00")
        BigDecimal discountAmount,
        
        @Schema(description = "ยอดรวมสุทธิ", example = "1170.00")
        BigDecimal totalPrice
) {}