package com.example.petshotel.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal revenueThisMonth,
        long bookingsThisMonth,
        long checkedInPets,
        long availableRoomCountToday,
        List<RecentBookingResponse> recentBookings,
        int selectedYear,
        List<MonthlyRevenueResponse> monthlyRevenue,
        BigDecimal yearlyRevenue
) {}