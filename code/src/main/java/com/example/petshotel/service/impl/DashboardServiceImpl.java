package com.example.petshotel.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.Receipt;
import com.example.petshotel.repository.BookingRepository;
import com.example.petshotel.repository.ReceiptRepository;
import com.example.petshotel.service.DashboardService;

@Service 
public class DashboardServiceImpl implements DashboardService{
    private final ReceiptRepository receiptRepository;
    private final BookingRepository bookingRepository;

    public DashboardServiceImpl(ReceiptRepository receiptRepository, BookingRepository bookingRepository) {
        this.receiptRepository = receiptRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override 
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue(){
        return receiptRepository.findAll().stream()
            .map(Receipt::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override 
    @Transactional(readOnly = true)
    public BigDecimal getRevenueBetween(LocalDate startDate, LocalDate endDate){
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return receiptRepository.findAll().stream()
            .filter(receipt -> receipt.getIssuedAt() != null &&
                    !receipt.getIssuedAt().isBefore(startDateTime) &&
                    !receipt.getIssuedAt().isAfter(endDateTime))
            .map(Receipt::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override 
    @Transactional(readOnly = true)
    public Map<String, Long> getBookingStatsByStatus(){
        return bookingRepository.findAll().stream()
                .filter(booking -> booking.getStatus() != null)
                .collect(Collectors.groupingBy(
                    booking -> booking.getStatus().name(),
                    Collectors.counting()
                ));
    }

    @Override 
    @Transactional(readOnly = true)
    public long getCurrentOccupancyCount(){
        LocalDate today = LocalDate.now();
        return bookingRepository.findAll().stream()
                .filter(booking -> booking.getCheckInDate() != null
                    && booking.getCheckOutDate() != null)
                .filter(booking -> !today.isBefore(booking.getCheckInDate()) &&
                                    !today.isAfter(booking.getCheckOutDate())).count();          
    }
}
