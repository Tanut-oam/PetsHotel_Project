package com.example.petshotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.BookingPet;
import com.example.petshotel.domain.entity.Pet;
import com.example.petshotel.domain.entity.Receipt;
import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.dto.response.DashboardResponse;
import com.example.petshotel.dto.response.MonthlyRevenueResponse;
import com.example.petshotel.dto.response.RecentBookingResponse;
import com.example.petshotel.mapper.RecentBookingMapper;
import com.example.petshotel.repository.BookingRepository;
import com.example.petshotel.repository.PetRepository;
import com.example.petshotel.repository.ReceiptRepository;
import com.example.petshotel.service.impl.DashboardServiceImpl;
import com.example.petshotel.domain.enums.PaymentStatus;

public class DashboardServiceTest {
    private ReceiptRepository receiptRepository;
    private BookingRepository bookingRepository;
    private DashboardServiceImpl dashboardService;
    private PetRepository petRepository;
    private AvailabilityService availabilityService;
    private RecentBookingMapper recentBookingMapper;

    @BeforeEach 
    void setUp(){
        receiptRepository = mock(ReceiptRepository.class);
        bookingRepository = mock(BookingRepository.class);
        petRepository = mock(PetRepository.class);
        availabilityService = mock(AvailabilityService.class);
        recentBookingMapper = new RecentBookingMapper();

        dashboardService = new DashboardServiceImpl(receiptRepository, bookingRepository, petRepository, availabilityService, recentBookingMapper);
    }

    private Receipt receipt(String amount, LocalDate paidDate, PaymentStatus paymentStatus) {

        Booking booking = new Booking();
        booking.setPaymentStatus(paymentStatus);

        if (paidDate != null) {
            booking.setPaidAt(paidDate.atStartOfDay());
        }

        Receipt receipt = new Receipt();
        receipt.setBooking(booking);
        receipt.setTotalAmount(new BigDecimal(amount));
        receipt.setIssuedAt(LocalDate.now().atStartOfDay());

        return receipt;
    }

    private Booking recentBooking(LocalDate createdDate){
        Booking booking = booking(createdDate, BookingStatus.CHECKED_IN, "A101", 2);
        booking.setId(25L);

        User customer = new User();
        customer.setFirstName("Nadia");
        customer.setLastName("Test");
        booking.setUser(customer);

        Pet firsPet = new Pet();
        firsPet.setName("Mochi");

        Pet secondPet = new Pet();
        secondPet.setName("Lily");

        booking.getBookingPets().get(0).setPet(firsPet);
        booking.getBookingPets().get(1).setPet(secondPet);

        return booking;
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
    void sumsOnlyPaidReceiptsFromCurrentMonthAndYear() {
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);

        when(receiptRepository.findAll()).thenReturn(List.of(
                receipt("1000.50", monthStart, PaymentStatus.PAID),
                receipt("250.25", monthStart, PaymentStatus.PAID),

                receipt("900.00", monthStart.minusDays(1),
                        PaymentStatus.PAID),

                receipt("800.00", monthStart.minusYears(1),
                        PaymentStatus.PAID),

                receipt("700.00", monthStart.plusMonths(1),
                        PaymentStatus.PAID),

                receipt("600.00", monthStart,
                        PaymentStatus.UNPAID),

                receipt("500.00", null,
                        PaymentStatus.PAID)
        ));

        BigDecimal result = dashboardService.getRevenueThisMonth();

        assertEquals(new BigDecimal("1250.75"), result);
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
        when(bookingRepository.count()).thenReturn(0L);
        when(petRepository.count()).thenReturn(0L);
        when(availabilityService.findAvailableRooms(
            any(LocalDate.class), any(LocalDate.class), eq(1)))
            .thenReturn(List.of());
        when(bookingRepository.findAll(any(Pageable.class)))
            .thenReturn(Page.<Booking>empty());

        DashboardResponse result = dashboardService.getDashboard();

        assertEquals(BigDecimal.ZERO, result.revenueThisMonth());
        assertEquals(0L, result.bookingsThisMonth());
        assertEquals(0L, result.checkedInPets());
        assertNull(result.topRoom());

        assertEquals(LocalDate.now().getYear(), result.selectedYear());
        assertEquals(12, result.monthlyRevenue().size());
        assertEquals(BigDecimal.ZERO, result.yearlyRevenue());

        assertEquals(0L, result.totalBookings());
        assertEquals(0L, result.availableRoomCountToday());
        assertEquals(0L, result.totalPets());
        assertTrue(result.recentBookings().isEmpty());
    }

    @Test 
    void combinesResultsIntoDashboardResponse(){
        LocalDate today = LocalDate.now();
        Booking latestBooking = recentBooking(today);

        when(receiptRepository.findAll())
        .thenReturn(List.of(
                receipt("1500.00", today, PaymentStatus.PAID)));
        when(bookingRepository.findAll())
                .thenReturn(List.of(latestBooking));
        when(bookingRepository.count()).thenReturn(1L);
        when(petRepository.count()).thenReturn(8L);

        when(availabilityService.findAvailableRooms(
                any(LocalDate.class), any(LocalDate.class), eq(1)))
                .thenReturn(List.of(new Room(), new Room()));

        when(bookingRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(latestBooking)));

        DashboardResponse result = dashboardService.getDashboard();

        assertEquals(new BigDecimal("1500.00"), result.revenueThisMonth());
        assertEquals(1L, result.bookingsThisMonth());
        assertEquals(2L, result.checkedInPets());
        assertEquals("A101", result.topRoom());

        assertEquals(1L, result.totalBookings());
        assertEquals(2L, result.availableRoomCountToday());
        assertEquals(8L, result.totalPets());

        assertEquals(today.getYear(), result.selectedYear());
        assertEquals(12, result.monthlyRevenue().size());
        assertEquals(new BigDecimal("1500.00"), result.yearlyRevenue());

        assertEquals(1, result.recentBookings().size());

        RecentBookingResponse recent = result.recentBookings().get(0);
        assertEquals(Long.valueOf(25L), recent.id());
        assertEquals("Nadia Test", recent.customerName());
        assertEquals(List.of("Mochi", "Lily"), recent.petNames());
        assertEquals("A101", recent.roomNumber());
        assertEquals(BookingStatus.CHECKED_IN, recent.status());

    }

    @Test
    void countsAvailableRoomsForTodayUntilTomorrow() {
        LocalDate beforeCall = LocalDate.now();

        when(availabilityService.findAvailableRooms(
                any(LocalDate.class), any(LocalDate.class), eq(1)))
                .thenReturn(List.of(new Room(), new Room()));

        long result = dashboardService.getAvailableRoomCountToday();

        LocalDate afterCall = LocalDate.now();

        ArgumentCaptor<LocalDate> checkInCaptor =
                ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> checkOutCaptor =
                ArgumentCaptor.forClass(LocalDate.class);

        verify(availabilityService).findAvailableRooms(
                checkInCaptor.capture(),
                checkOutCaptor.capture(),
                eq(1));

        LocalDate checkIn = checkInCaptor.getValue();

        assertEquals(2L, result);
        assertTrue(checkIn.equals(beforeCall) || checkIn.equals(afterCall));
        assertEquals(checkIn.plusDays(1), checkOutCaptor.getValue());
    }

    @Test
    void requestsFiveLatestBookingsOrderedByCreatedAtThenId() {
        when(bookingRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.<Booking>empty());

        dashboardService.getRecentBookings();

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(bookingRepository).findAll(captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());

        List<Sort.Order> orders = pageable.getSort().toList();

        assertEquals(2, orders.size());

        assertEquals("createdAt", orders.get(0).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(0).getDirection());

        assertEquals("id", orders.get(1).getProperty());
        assertEquals(Sort.Direction.DESC, orders.get(1).getDirection());
    }

    @Test
    void countsOnlyBookingsCreatedInCurrentMonthAndYear() {
        LocalDate thisMonth = LocalDate.now().withDayOfMonth(1);

        when(bookingRepository.findAll()).thenReturn(List.of(
                booking(thisMonth, BookingStatus.CONFIRMED, "A101", 0),
                booking(thisMonth, BookingStatus.CANCELLED, "A102", 0),
                booking(thisMonth.minusMonths(1),
                        BookingStatus.CONFIRMED, "A103", 0),
                booking(thisMonth.minusYears(1),
                        BookingStatus.CONFIRMED, "A104", 0),
                booking(null, BookingStatus.CONFIRMED, "A105", 0)
        ));

        long result = dashboardService.getBookingsThisMonth();

        assertEquals(1L, result);
    }

    @Test
    void groupsPaidRevenueByMonthForSelectedYear() {
        when(receiptRepository.findAll()).thenReturn(List.of(
                receipt("1000.50", LocalDate.of(2025, 1, 1),
                        PaymentStatus.PAID),
                receipt("250.25", LocalDate.of(2025, 1, 31),
                        PaymentStatus.PAID),
                receipt("600.00", LocalDate.of(2025, 2, 10),
                        PaymentStatus.PAID),
                receipt("100.00", LocalDate.of(2025, 12, 31),
                        PaymentStatus.PAID),

                receipt("900.00", LocalDate.of(2024, 12, 31),
                        PaymentStatus.PAID),
                receipt("800.00", LocalDate.of(2026, 1, 1),
                        PaymentStatus.PAID),
                receipt("700.00", LocalDate.of(2025, 7, 1),
                        PaymentStatus.UNPAID),
                receipt("500.00", null, PaymentStatus.PAID)
        ));

        when(bookingRepository.findAll()).thenReturn(List.of());

        when(bookingRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.<Booking>empty());

        when(availabilityService.findAvailableRooms(
                any(LocalDate.class), any(LocalDate.class), eq(1)))
                .thenReturn(List.of());

        DashboardResponse result = dashboardService.getDashboard(2025);

        assertEquals(2025, result.selectedYear());
        assertEquals(12, result.monthlyRevenue().size());

        for (int index = 0; index < 12; index++) {
            MonthlyRevenueResponse month = result.monthlyRevenue().get(index);
            assertEquals(index + 1, month.month());
        }

        assertEquals("มกราคม",
                result.monthlyRevenue().get(0).monthName());
        assertEquals(new BigDecimal("1250.75"),
                result.monthlyRevenue().get(0).totalAmount());
        assertEquals(new BigDecimal("600.00"),
                result.monthlyRevenue().get(1).totalAmount());
        assertEquals(BigDecimal.ZERO,
                result.monthlyRevenue().get(2).totalAmount());
        assertEquals(BigDecimal.ZERO,
                result.monthlyRevenue().get(6).totalAmount());
        assertEquals(new BigDecimal("100.00"),
                result.monthlyRevenue().get(11).totalAmount());

        assertEquals(new BigDecimal("1950.75"), result.yearlyRevenue());
    }

    @Test
    void returnsTwelveZeroMonthsWhenSelectedYearHasNoRevenue() {
        when(receiptRepository.findAll()).thenReturn(List.of(
                receipt("900.00", LocalDate.of(2024, 12, 31),
                        PaymentStatus.PAID)
        ));

        List<MonthlyRevenueResponse> result =
                dashboardService.getMonthlyRevenue(2025);

        assertEquals(12, result.size());

        for (MonthlyRevenueResponse month : result) {
            assertEquals(BigDecimal.ZERO, month.totalAmount());
        }
    }

}
