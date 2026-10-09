package com.example.petshotel.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

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
import com.example.petshotel.repository.ReceiptRepository;
import com.example.petshotel.service.AvailabilityService;
import com.example.petshotel.service.DashboardService;
import com.example.petshotel.domain.enums.PaymentStatus;
import com.example.petshotel.dto.response.MonthlyRevenueResponse;
import com.example.petshotel.dto.response.RevenueBookingResponse;
import com.example.petshotel.mapper.RevenueBookingMapper;

@Service 
public class DashboardServiceImpl implements DashboardService{
    private final ReceiptRepository receiptRepository;
    private final BookingRepository bookingRepository;
    private final AvailabilityService availabilityService;
    private final RecentBookingMapper recentBookingMapper;
    private final RevenueBookingMapper revenueBookingMapper;

    public DashboardServiceImpl(ReceiptRepository receiptRepository, BookingRepository bookingRepository,
        AvailabilityService availabilityService,
        RecentBookingMapper recentBookingMapper,
        RevenueBookingMapper revenueBookingMapper) {
        this.receiptRepository = receiptRepository;
        this.bookingRepository = bookingRepository;
        this.availabilityService = availabilityService;
        this.recentBookingMapper = recentBookingMapper;
        this.revenueBookingMapper = revenueBookingMapper;
    }


    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRevenueThisMonth() {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate nextMonthStart = monthStart.plusMonths(1);

        BigDecimal total = BigDecimal.ZERO;

        for (Receipt receipt : receiptRepository.findAll()) {
            Booking booking = receipt.getBooking();

            if (booking == null
                    || booking.getPaymentStatus() != PaymentStatus.PAID
                    || booking.getPaidAt() == null) {
                continue;
            }

            LocalDate paidDate = booking.getPaidAt().toLocalDate();

            if (!paidDate.isBefore(monthStart)
                    && paidDate.isBefore(nextMonthStart)) {
                total = total.add(receipt.getTotalAmount());
            }
        }

        return total;
    }

    @Override
    @Transactional(readOnly = true)
    public long getBookingsThisMonth() {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LocalDate nextMonthStart = monthStart.plusMonths(1);
        long count = 0;

        for (Booking booking : bookingRepository.findAll()) {
            if (booking.getStatus() == BookingStatus.CANCELLED) {
                continue;
            }

            LocalDate checkIn = booking.getCheckInDate();
            LocalDate checkOut = booking.getCheckOutDate();

            if (checkIn != null
                    && checkOut != null
                    && checkOut.isAfter(checkIn)
                    && checkIn.isBefore(nextMonthStart)
                    && checkOut.isAfter(monthStart)) {
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
    public long getAvailableRoomCountToday(){
        LocalDate today = LocalDate.now();
        return availabilityService.findAvailableRooms(today, today.plusDays(1), 1).size();
    }

    @Override 
    @Transactional(readOnly = true)
    public List<RecentBookingResponse> getRecentBookings(){
        PageRequest pageRequest = PageRequest.of(
            0, 5, Sort.by(Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")));
        List<Booking> bookings = bookingRepository.findAll(pageRequest).getContent();
        List<RecentBookingResponse> responses = new ArrayList<>();
        for(Booking booking : bookings){
            responses.add(recentBookingMapper.toResponse(booking));
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(int year) {
        List<MonthlyRevenueResponse> monthlyRevenue =
                getMonthlyRevenue(year);

        BigDecimal yearlyRevenue = BigDecimal.ZERO;

        for (MonthlyRevenueResponse month : monthlyRevenue) {
            yearlyRevenue = yearlyRevenue.add(month.totalAmount());
        }

        return new DashboardResponse(
                getRevenueThisMonth(),
                getBookingsThisMonth(),
                getCheckedInPets(),
                getAvailableRoomCountToday(),
                getRecentBookings(),
                year,
                monthlyRevenue,
                yearlyRevenue
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyRevenueResponse> getMonthlyRevenue(int year) {
        if (year < 1 || year > 9999) {
            throw new IllegalArgumentException(
                    "Year must be between 1 and 9999");
        }

        String[] monthNames = {
                "มกราคม", "กุมภาพันธ์", "มีนาคม",
                "เมษายน", "พฤษภาคม", "มิถุนายน",
                "กรกฎาคม", "สิงหาคม", "กันยายน",
                "ตุลาคม", "พฤศจิกายน", "ธันวาคม"
        };

        BigDecimal[] monthlyTotals = new BigDecimal[12];

        List<List<RevenueBookingResponse>> monthlyBookings =
                new ArrayList<>();

        for (int index = 0; index < 12; index++) {
            monthlyTotals[index] = BigDecimal.ZERO;
            monthlyBookings.add(new ArrayList<>());
        }

        for (Receipt receipt : receiptRepository.findAll()) {
            Booking booking = receipt.getBooking();

            if (booking == null
                    || booking.getPaymentStatus() != PaymentStatus.PAID
                    || booking.getPaidAt() == null) {
                continue;
            }

            LocalDate paidDate = booking.getPaidAt().toLocalDate();

            if (paidDate.getYear() != year) {
                continue;
            }

            int monthIndex = paidDate.getMonthValue() - 1;

            monthlyTotals[monthIndex] = monthlyTotals[monthIndex]
                    .add(receipt.getTotalAmount());

            RevenueBookingResponse response =
                    revenueBookingMapper.toResponse(receipt);

            monthlyBookings.get(monthIndex).add(response);
        }

        List<MonthlyRevenueResponse> months = new ArrayList<>();

        for (int index = 0; index < 12; index++) {
            List<RevenueBookingResponse> bookingsOfMonth =
                    monthlyBookings.get(index);

            bookingsOfMonth.sort(new Comparator<RevenueBookingResponse>() {
                @Override
                public int compare(
                        RevenueBookingResponse first,
                        RevenueBookingResponse second) {

                    return first.paidAt().compareTo(second.paidAt());
                }
            });

            months.add(new MonthlyRevenueResponse(
                    index + 1,
                    monthNames[index],
                    monthlyTotals[index],
                    List.copyOf(bookingsOfMonth)
            ));
        }

        return months;
    }



}
