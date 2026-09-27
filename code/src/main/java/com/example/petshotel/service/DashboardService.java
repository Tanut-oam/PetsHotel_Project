package com.example.petshotel.service;

import java.math.BigDecimal;
import java.util.List;

import com.example.petshotel.dto.response.DashboardResponse;
import com.example.petshotel.dto.response.RecentBookingResponse;

public interface DashboardService {
    BigDecimal getTotalRevenue();
    long getBookingsThisMonth();
    long getCheckedInPets();
    String getTopRoom();
    long getTotalBookings();
    long getAvailableRoomCountToday();
    long getTotalPets();
    List<RecentBookingResponse> getRecentBookings();
    DashboardResponse getDashboard();     
}
