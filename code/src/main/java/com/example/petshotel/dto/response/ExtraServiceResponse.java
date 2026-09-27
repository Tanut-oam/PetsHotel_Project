package com.example.petshotel.dto.response;

import java.math.BigDecimal;

public record ExtraServiceResponse(
    Long id,
    String name,
    String description,
    BigDecimal price,
    Boolean active
) {
}
