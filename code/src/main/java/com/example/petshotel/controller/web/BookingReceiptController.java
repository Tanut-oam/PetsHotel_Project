package com.example.petshotel.controller.web;

import java.security.Principal;
import java.util.Objects;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.UserRole;
import com.example.petshotel.dto.response.BookingResponse;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.BookingService;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.ReceiptService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class BookingReceiptController {

    private final BookingService bookingService;
    private final CurrentUserService currentUserService;
    private final ReceiptService receiptService;

    @GetMapping("/bookings/{id}/receipt")
    public String showReceipt(
            @PathVariable("id") Long id,
            Principal principal,
            Model model) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User user;

        try {
            user = currentUserService.getByEmail(principal.getName());
        } catch (ResourceNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        BookingResponse booking;

        try {
            booking = bookingService.getBookingById(id);
        } catch (ResourceNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        boolean staff = user.getRole() == UserRole.STAFF
                || user.getRole() == UserRole.ADMIN;

        if (!staff && !Objects.equals(user.getId(), booking.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        var receipt = receiptService.getReceiptByBookingId(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("booking", booking);
        model.addAttribute("receipt", receipt);
        model.addAttribute(
                "backUrl",
                staff ? "/admin/bookings" : "/bookings/" + id
        );

        return "booking-receipt";
    }
}