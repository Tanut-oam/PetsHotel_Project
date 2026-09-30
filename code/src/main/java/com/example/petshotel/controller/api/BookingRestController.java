package com.example.petshotel.controller.api;

import com.example.petshotel.dto.request.CreateBookingRequest;
import com.example.petshotel.dto.response.BookingResponse;
import com.example.petshotel.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingRestController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {

        BookingResponse response = bookingService.createBooking(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<BookingResponse> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/{id}")
    public BookingResponse getBookingById(@PathVariable("id") Long id) {
        return bookingService.getBookingById(id);
    }

    @PostMapping("/{id}/confirm")
    public BookingResponse confirmBooking(@PathVariable("id") Long id) {
        return bookingService.confirmBooking(id);
    }

    @PostMapping("/{id}/check-in")
    public BookingResponse checkIn(@PathVariable("id") Long id) {
        return bookingService.checkIn(id);
    }

    @PostMapping("/{id}/check-out")
    public BookingResponse checkOut(@PathVariable("id") Long id) {
        return bookingService.checkOut(id);
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancelBooking(@PathVariable("id") Long id) {
        return bookingService.cancelBooking(id);
    }
}