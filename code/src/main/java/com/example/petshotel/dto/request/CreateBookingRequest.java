package com.example.petshotel.dto.request;

import lombok.*;
import java.util.Map;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookingRequest {

    private Long userId;

    private Long roomId;

    private List<Long> petIds;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Map<Long, Integer> extraServiceQuantities;

    private Long promotionId;
}