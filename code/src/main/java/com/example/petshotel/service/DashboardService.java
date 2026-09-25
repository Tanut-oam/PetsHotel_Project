package com.example.petshotel.service;

import java.math.BigDecimal;

import com.example.petshotel.dto.response.DashboardResponse;

public interface DashboardService {
    BigDecimal getTotalRevenue();
    long getBookingsThisMonth();
    long getCheckedInPets();
    String getTopRoom();
    DashboardResponse getDashboard();     
}
