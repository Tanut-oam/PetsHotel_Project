package com.example.petshotel.dto.request;
import jakarta.validation.constraints.NotNull;

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

    @NotNull(message = "User ID ต้องไม่เป็นค่าว่าง")
    private Long userId;

    @NotNull(message = "Room ID ต้องไม่เป็นค่าว่าง")
    private Long roomId;

    private List<Long> petIds;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Map<Long, Integer> extraServiceQuantities;

    private Long promotionId;
}