package com.example.petshotel.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
    BigDecimal revenueThisMonth,
    long bookingsThisMonth,
    long checkedInPets,
    String topRoom,
    long totalBookings,
    long availableRoomCountToday,
    long totalPets,
    List<RecentBookingResponse> recentBookings
) {}