package com.example.petshotel.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.Receipt;
import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.dto.response.DashboardResponse;
import com.example.petshotel.dto.response.RecentBookingResponse;
import com.example.petshotel.mapper.RecentBookingMapper;
import com.example.petshotel.repository.BookingRepository;
import com.example.petshotel.repository.PetRepository;
import com.example.petshotel.repository.ReceiptRepository;
import com.example.petshotel.service.AvailabilityService;
import com.example.petshotel.service.DashboardService;

@Service 
public class DashboardServiceImpl implements DashboardService{
    private final ReceiptRepository receiptRepository;
    private final BookingRepository bookingRepository;
    private final PetRepository petRepository;
    private final AvailabilityService availabilityService;
    private final RecentBookingMapper recentBookingMapper;

    public DashboardServiceImpl(ReceiptRepository receiptRepository, BookingRepository bookingRepository,
        PetRepository petRepository,
        AvailabilityService availabilityService,
        RecentBookingMapper recentBookingMapper) {
        this.receiptRepository = receiptRepository;
        this.bookingRepository = bookingRepository;
        this.petRepository = petRepository;
        this.availabilityService = availabilityService;
        this.recentBookingMapper = recentBookingMapper;
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
                Long previousCount = roomBooking.get(roomNumber);

                if (previousCount == null) {
                    roomBooking.put(roomNumber, 1L);
                } else {
                    roomBooking.put(roomNumber, previousCount + 1L);
                }
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
    public long getTotalBookings() {
        return bookingRepository.count();
    }

    @Override 
    @Transactional(readOnly = true)
    public long getAvailableRoomCountToday(){
        LocalDate today = LocalDate.now();
        return availabilityService.findAvailableRooms(today, today.plusDays(1), 1).size();
    }

    @Override 
    @Transactional(readOnly = true)
    public long getTotalPets() {
        return petRepository.count();
    }

    @Override 
    @Transactional(readOnly = true)
    public List<RecentBookingResponse> getRecentBookings(){
        PageRequest pageRequest = PageRequest.of(
            0, 5, Sort.by(Sort.Direction.DESC, Booking::getCreatedAt, Booking::getId));
        List<Booking> bookings = bookingRepository.findAll(pageRequest).getContent();
        List<RecentBookingResponse> responses = new ArrayList<>();
        for(Booking booking : bookings){
            responses.add(recentBookingMapper.toResponse(booking));
        }
        return responses;
    }

    @Override 
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(){
        return new DashboardResponse(
            getTotalRevenue(),
            getBookingsThisMonth(),
            getCheckedInPets(),
            getTopRoom(),
            getTotalBookings(),
            getAvailableRoomCountToday(),
            getTotalPets(),
            getRecentBookings()
        );
    }



}
