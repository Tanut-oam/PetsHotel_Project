package com.example.petshotel.service;

import com.example.petshotel.dto.request.CreateBookingRequest;
import com.example.petshotel.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request);

    BookingResponse getBookingById(Long id);

    List<BookingResponse> getAllBookings();

    BookingResponse confirmBooking(Long id);

    BookingResponse checkIn(Long id);

    BookingResponse checkOut(Long id);

    BookingResponse cancelBooking(Long id);
}