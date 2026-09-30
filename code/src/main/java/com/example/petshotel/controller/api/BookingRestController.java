package com.example.petshotel.controller.api;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.UserRole;
import com.example.petshotel.dto.request.CreateBookingApiRequest;
import com.example.petshotel.dto.request.CreateBookingRequest;
import com.example.petshotel.dto.response.BookingResponse;
import com.example.petshotel.repository.UserRepository;
import com.example.petshotel.service.BookingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingRestController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingApiRequest request,
            Principal principal) {

        User user = currentUser(principal);

        CreateBookingRequest serviceRequest = CreateBookingRequest.builder()
                .userId(user.getId())
                .roomId(request.roomId())
                .petIds(request.petIds())
                .checkInDate(request.checkInDate())
                .checkOutDate(request.checkOutDate())
                .extraServiceQuantities(request.extraServiceQuantities())
                .promotionId(request.promotionId())
                .build();

        BookingResponse response =
                bookingService.createBooking(serviceRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<BookingResponse> getAllBookings(Principal principal) {
        requireStaffOrAdmin(currentUser(principal));

        return bookingService.getAllBookings();
    }

    @GetMapping("/{id}")
    public BookingResponse getBookingById(
            @PathVariable("id") Long id,
            Principal principal) {

        User user = currentUser(principal);
        BookingResponse booking = bookingService.getBookingById(id);

        requireOwnerOrStaff(user, booking);

        return booking;
    }

    @PostMapping("/{id}/confirm")
    public BookingResponse confirmBooking(
            @PathVariable("id") Long id,
            Principal principal) {

        requireStaffOrAdmin(currentUser(principal));

        return bookingService.confirmBooking(id);
    }

    @PostMapping("/{id}/check-in")
    public BookingResponse checkIn(
            @PathVariable("id") Long id,
            Principal principal) {

        requireStaffOrAdmin(currentUser(principal));

        return bookingService.checkIn(id);
    }

    @PostMapping("/{id}/check-out")
    public BookingResponse checkOut(
            @PathVariable("id") Long id,
            Principal principal) {

        requireStaffOrAdmin(currentUser(principal));

        return bookingService.checkOut(id);
    }

    @PostMapping("/{id}/cancel")
    public BookingResponse cancelBooking(
            @PathVariable("id") Long id,
            Principal principal) {

        User user = currentUser(principal);
        BookingResponse booking = bookingService.getBookingById(id);

        requireOwnerOrStaff(user, booking);

        return bookingService.cancelBooking(id);
    }

    private User currentUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login is required"
            );
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"
                ));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new AccessDeniedException("Account is inactive");
        }

        return user;
    }

    private boolean isStaffOrAdmin(User user) {
        return user.getRole() == UserRole.STAFF
                || user.getRole() == UserRole.ADMIN;
    }

    private void requireStaffOrAdmin(User user) {
        if (!isStaffOrAdmin(user)) {
            throw new AccessDeniedException(
                    "This operation requires STAFF or ADMIN"
            );
        }
    }

    private void requireOwnerOrStaff(
            User user,
            BookingResponse booking) {

        boolean owner = Objects.equals(
                user.getId(),
                booking.getUserId()
        );

        if (!owner && !isStaffOrAdmin(user)) {
            throw new AccessDeniedException(
                    "You cannot access another customer's booking"
            );
        }
    }
}