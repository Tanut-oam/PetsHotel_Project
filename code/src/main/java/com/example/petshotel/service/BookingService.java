package com.example.petshotel.service;

import com.example.petshotel.dto.request.CreateBookingRequest;
import com.example.petshotel.dto.response.BookingResponse;
import com.example.petshotel.dto.response.BookingPriceResponse;
import com.example.petshotel.dto.response.PetAvailabilityResponse;

import java.util.List;
import java.time.LocalDate;

public interface BookingService {

    PetAvailabilityResponse getPetAvailability(
        Long userId,
        LocalDate checkInDate,
        LocalDate checkOutDate
    );

    BookingPriceResponse previewPrice(CreateBookingRequest request);

    BookingResponse createBooking(CreateBookingRequest request);

    BookingResponse getBookingById(Long id);

    List<BookingResponse> getBookingsByUserId(Long userId);

    List<BookingResponse> getAllBookings();

    BookingResponse confirmBooking(Long id);

    BookingResponse checkIn(Long id);

    BookingResponse checkOut(Long id);

    BookingResponse cancelBooking(Long id);

}
