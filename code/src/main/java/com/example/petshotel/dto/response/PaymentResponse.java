package com.example.petshotel.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.petshotel.domain.enums.PaymentStatus;

public record PaymentResponse(
    Long bookingId,
    PaymentStatus paymentStatus,
    BigDecimal paidAmount,
    LocalDateTime paidAt,
    Long receiptId,
    String receiptNumber

) {
    
}
