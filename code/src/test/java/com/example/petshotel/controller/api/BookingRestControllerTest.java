package com.example.petshotel.controller.api;

import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.dto.request.CreateBookingRequest;
import com.example.petshotel.dto.response.BookingResponse;
import com.example.petshotel.exception.GlobalExceptionHandler;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.BookingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookingRestControllerTest {

    private BookingService bookingService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        bookingService = mock(BookingService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new BookingRestController(bookingService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createBookingShouldReturnCreatedAndPassRequestToService()
            throws Exception {

        when(bookingService.createBooking(any(CreateBookingRequest.class)))
                .thenReturn(response(BookingStatus.PENDING));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": 10,
                                    "roomId": 20,
                                    "petIds": [30],
                                    "checkInDate": "2026-10-10",
                                    "checkOutDate": "2026-10-12"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalPrice").value(1000));

        ArgumentCaptor<CreateBookingRequest> captor =
                ArgumentCaptor.forClass(CreateBookingRequest.class);

        verify(bookingService).createBooking(captor.capture());

        CreateBookingRequest request = captor.getValue();
        assertEquals(10L, request.getUserId());
        assertEquals(20L, request.getRoomId());
        assertEquals(List.of(30L), request.getPetIds());
        assertEquals(LocalDate.of(2026, 10, 10), request.getCheckInDate());
        assertEquals(LocalDate.of(2026, 10, 12), request.getCheckOutDate());
    }

    @Test
    void createBookingWithMissingFieldsShouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void createBookingWithNullPetIdShouldReturnBadRequest()
            throws Exception {

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "userId": 10,
                                    "roomId": 20,
                                    "petIds": [null],
                                    "checkInDate": "2026-10-10",
                                    "checkOutDate": "2026-10-12"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void getAllBookingsShouldReturnList() throws Exception {
        when(bookingService.getAllBookings())
                .thenReturn(List.of(response(BookingStatus.PENDING)));

        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(bookingService).getAllBookings();
    }

    @Test
    void getBookingByIdShouldReturnBooking() throws Exception {
        when(bookingService.getBookingById(1L))
                .thenReturn(response(BookingStatus.CONFIRMED));

        mockMvc.perform(get("/api/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).getBookingById(1L);
    }

    @Test
    void getMissingBookingShouldReturnNotFound() throws Exception {
        when(bookingService.getBookingById(99L))
                .thenThrow(new ResourceNotFoundException("Booking", 99L));

        mockMvc.perform(get("/api/bookings/99"))
                .andExpect(status().isNotFound());

        verify(bookingService).getBookingById(99L);
    }

    @Test
    void invalidBookingIdShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/bookings/abc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(bookingService);
    }

    @Test
    void confirmBookingShouldReturnConfirmedBooking() throws Exception {
        when(bookingService.confirmBooking(1L))
                .thenReturn(response(BookingStatus.CONFIRMED));

        mockMvc.perform(post("/api/bookings/1/confirm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).confirmBooking(1L);
    }

    @Test
    void checkInShouldReturnCheckedInBooking() throws Exception {
        when(bookingService.checkIn(1L))
                .thenReturn(response(BookingStatus.CHECKED_IN));

        mockMvc.perform(post("/api/bookings/1/check-in"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_IN"));

        verify(bookingService).checkIn(1L);
    }

    @Test
    void checkOutShouldReturnCheckedOutBooking() throws Exception {
        when(bookingService.checkOut(1L))
                .thenReturn(response(BookingStatus.CHECKED_OUT));

        mockMvc.perform(post("/api/bookings/1/check-out"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CHECKED_OUT"));

        verify(bookingService).checkOut(1L);
    }

    @Test
    void cancelBookingShouldReturnCancelledBooking() throws Exception {
        when(bookingService.cancelBooking(1L))
                .thenReturn(response(BookingStatus.CANCELLED));

        mockMvc.perform(post("/api/bookings/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(bookingService).cancelBooking(1L);
    }

    @Test
    void cancellationRejectedByServiceShouldReturnConflict()
            throws Exception {

        when(bookingService.cancelBooking(1L))
                .thenThrow(new IllegalStateException(
                        "Paid bookings cannot be cancelled through this operation"
                ));

        mockMvc.perform(post("/api/bookings/1/cancel"))
                .andExpect(status().isConflict());

        verify(bookingService).cancelBooking(1L);
    }

    private BookingResponse response(BookingStatus bookingStatus) {
        return BookingResponse.builder()
                .id(1L)
                .userId(10L)
                .roomId(20L)
                .petIds(List.of(30L))
                .checkInDate(LocalDate.of(2026, 10, 10))
                .checkOutDate(LocalDate.of(2026, 10, 12))
                .status(bookingStatus)
                .totalPrice(new BigDecimal("1000.00"))
                .build();
    }
}