package com.example.petshotel.dto.response;

import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;

    private Long userId;

    private Long roomId;

    private List<Long> petIds;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private BookingStatus status;

    private BigDecimal totalPrice;

    private PaymentStatus paymentStatus;
}