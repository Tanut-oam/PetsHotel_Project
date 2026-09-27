package com.example.petshotel.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.Receipt;
import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.dto.response.DashboardResponse;
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
    public BigDecimal getTotalRevenue() {
        BigDecimal total = BigDecimal.ZERO;
        for (Receipt receipt : receiptRepository.findAll()) {
            total = total.add(receipt.getTotalAmount());
        }
        return total;
    }

    @Override 
    @Transactional(readOnly = true)
    public long getBookingsThisMonth(){
        LocalDate today = LocalDate.now();
        long count = 0;

        for(Booking booking : bookingRepository.findAll()){
            if (booking.getCreatedAt() != null
                && booking.getCreatedAt().getYear() == today.getYear()
                && booking.getCreatedAt().getMonth() == today.getMonth()) {
                count++;
            }
        }
        return count;
    }

    @Override 
    @Transactional(readOnly = true)
    public long getCheckedInPets(){
        long count = 0;

        for(Booking booking : bookingRepository.findAll()){
            if (booking.getStatus() == BookingStatus.CHECKED_IN) {
                count += booking.getBookingPets().size();
            }
        }
        return count;
    }

    @Override 
    @Transactional(readOnly = true)
    public String getTopRoom(){
        Map<String, Long> roomBooking = new HashMap<>();

        for(Booking booking : bookingRepository.findAll()){
            if (booking.getStatus() != BookingStatus.CANCELLED
                && booking.getRoom() != null) {
                String roomNumber = booking.getRoom().getRoomNumber();
                roomBooking.merge(roomNumber, 1L, Long::sum);
            }
        }

        String topRoom = null;
        long highestCount = 0;

        for(Map.Entry<String, Long> entry : roomBooking.entrySet()){
            if (entry.getValue() > highestCount 
                    || ( entry.getValue() == highestCount
                        && (topRoom == null || entry.getKey().compareTo(topRoom)< 0 ))) {
                topRoom = entry.getKey();
                highestCount = entry.getValue();
            }   
        }
        return  topRoom;
    }

    @Override 
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(){
        return new DashboardResponse(
            getTotalRevenue(),
            getBookingsThisMonth(),
            getCheckedInPets(),
            getTopRoom()
        );
    }



}
