package com.example.petshotel.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public interface DashboardService {
    BigDecimal getTotalRevenue();
    BigDecimal getRevenueBetween(LocalDate startDate, LocalDate endDate);
    Map<String, Long> getBookingStatsByStatus();
    long getCurrentOccupancyCount();           
}
