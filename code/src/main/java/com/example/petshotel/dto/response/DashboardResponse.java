package com.example.petshotel.dto.response;

import java.math.BigDecimal;

public record DashboardResponse(
    BigDecimal totalRevenue,
    long bookingsThisMonth,
    long checkedInPets,
    String topRoom
) {}