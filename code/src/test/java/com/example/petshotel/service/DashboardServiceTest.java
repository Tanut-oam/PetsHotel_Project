package com.example.petshotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.BookingPet;
import com.example.petshotel.domain.entity.Receipt;
import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.dto.response.DashboardResponse;
import com.example.petshotel.repository.BookingRepository;
import com.example.petshotel.repository.ReceiptRepository;
import com.example.petshotel.service.impl.DashboardServiceImpl;

public class DashboardServiceTest {
    private ReceiptRepository receiptRepository;
    private BookingRepository bookingRepository;
    private DashboardServiceImpl dashboardService;

    @BeforeEach 
    void setUp(){
        receiptRepository = mock(ReceiptRepository.class);
        bookingRepository = mock(BookingRepository.class);

        dashboardService = new DashboardServiceImpl(receiptRepository, bookingRepository);
    }

    private Receipt receipt(String amount){
        Receipt receipt = new Receipt();
        receipt.setTotalAmount(new BigDecimal(amount));
        return receipt;
    }

    private Booking booking(LocalDate createdDate, BookingStatus status, String roomNumber, int petCount){
        Room room = new Room();
        room.setRoomNumber(roomNumber);

        Booking booking = new Booking();
        booking.setStatus(status);
        booking.setRoom(room);

        if (createdDate != null) {
            booking.setCreatedAt(createdDate.atStartOfDay());
        }

        for(int i=0; i<petCount; i++){
            BookingPet bookingPet = new BookingPet();
            bookingPet.setBooking(booking);
            booking.getBookingPets().add(bookingPet);
        }

        return booking;
    }

    @Test 
    void sumsRevenueFromReceipts(){
        when(receiptRepository.findAll()).thenReturn(List.of(
            receipt("1000.50"),receipt("250.25")
        ));

        BigDecimal result = dashboardService.getTotalRevenue();

        assertEquals(new BigDecimal("1250.75"), result);
    }

    @Test 
    void countsOnlyBookingsCreatedInCurrentMonthAndYear(){
        LocalDate thisMonth = LocalDate.now().withDayOfMonth(1);

        when(bookingRepository.findAll()).thenReturn(List.of(
            booking(thisMonth, BookingStatus.CONFIRMED, "A101", 0),
            booking(thisMonth, BookingStatus.CANCELLED, "A102", 0),
            booking(thisMonth.minusMonths(1), BookingStatus.CONFIRMED, "A103", 0),
            booking(thisMonth.minusYears(1), BookingStatus.CONFIRMED, "A104", 0),
            booking(null, BookingStatus.CONFIRMED, "A105", 0)
        ));

        long result = dashboardService.getBookingsThisMonth();

        assertEquals(2L, result);
    }

    @Test 
    void countsPetsOnlyInCheckedInBookings(){
        when(bookingRepository.findAll()).thenReturn(List.of(
            booking(null, BookingStatus.CHECKED_IN, "A101", 2),
            booking(null, BookingStatus.CHECKED_IN, "A102", 1),
            booking(null, BookingStatus.CONFIRMED, "A103", 4),
            booking(null, BookingStatus.CHECKED_OUT, "A104", 2),
            booking(null, BookingStatus.CANCELLED, "A105", 3)
        ));

        long result = dashboardService.getCheckedInPets();

        assertEquals(3L, result);
    }

    @Test 
    void findsTopRoomWithoutCountingCancelledBookings(){
        when(bookingRepository.findAll()).thenReturn(List.of(
            booking(null, BookingStatus.CONFIRMED, "A101", 0),
            booking(null, BookingStatus.CHECKED_OUT, "A101", 0),
            booking(null, BookingStatus.CONFIRMED, "B201", 0),
            booking(null, BookingStatus.CANCELLED, "B201", 0),
            booking(null, BookingStatus.CANCELLED, "B201", 0)
        ));

        String result = dashboardService.getTopRoom();

        assertEquals("A101", result);
    }

    @Test 
    void choosesAlphabeticallyFirstRoomWhenCountsAreEqual(){
        when(bookingRepository.findAll()).thenReturn(List.of(
            booking(null, BookingStatus.CONFIRMED, "B201", 0),
            booking(null, BookingStatus.CONFIRMED, "A101", 0)
        ));

        String result = dashboardService.getTopRoom();

        assertEquals("A101", result);
    }

    @Test 
    void returnsEmptyDashboardWhenThereIsNoData(){
        when(receiptRepository.findAll()).thenReturn(List.of());
        when(bookingRepository.findAll()).thenReturn(List.of());

        DashboardResponse result = dashboardService.getDashboard();

        assertEquals(BigDecimal.ZERO, result.totalRevenue());
        assertEquals(0L, result.bookingsThisMonth());
        assertEquals(0L, result.checkedInPets());
        assertNull(result.topRoom());
    }

    @Test 
    void combinesResultsIntoDashboardResponse(){
        LocalDate today = LocalDate.now();

        when(receiptRepository.findAll()).thenReturn(List.of(receipt("1500.00")));
        when(bookingRepository.findAll()).thenReturn(List.of(booking(today, BookingStatus.CHECKED_IN, "A101", 2)));

        DashboardResponse result = dashboardService.getDashboard();

        assertEquals(new BigDecimal("1500.00"), result.totalRevenue());
        assertEquals(1L, result.bookingsThisMonth());
        assertEquals(2L, result.checkedInPets());
        assertEquals("A101", result.topRoom());
    }
}
