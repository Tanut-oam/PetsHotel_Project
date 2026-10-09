package com.example.petshotel.service;

import java.math.BigDecimal;
import java.util.List;

import com.example.petshotel.dto.response.DashboardResponse;
import com.example.petshotel.dto.response.MonthlyRevenueResponse;
import com.example.petshotel.dto.response.RecentBookingResponse;

public interface DashboardService {
    BigDecimal getRevenueThisMonth();
    long getBookingsThisMonth();
    long getCheckedInPets();
    long getAvailableRoomCountToday();
    List<RecentBookingResponse> getRecentBookings();
    List<MonthlyRevenueResponse> getMonthlyRevenue(int year);
    DashboardResponse getDashboard(int year);   
}
