package com.example.petshotel.controller.web;

import java.security.Principal;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.UserRole;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.BookingService;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.PaymentService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;
    private final CurrentUserService currentUserService;
    private final PaymentService paymentService;

    @GetMapping
    public String showBookings(Principal principal, Model model) {
        requireStaff(principal);
        model.addAttribute("bookings", bookingService.getAllBookings());
        return "admin/bookings";
    }

    @PostMapping("/{id}/{action}")
    public String performAction(
            @PathVariable("id") Long id,
            @PathVariable("action") String action,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        requireStaff(principal);

        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        try {
            switch (action) {
                case "confirm" -> bookingService.confirmBooking(id);
                case "check-in" -> bookingService.checkIn(id);
                case "check-out" -> bookingService.checkOut(id);
                case "cancel" -> bookingService.cancelBooking(id);
                default -> throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND
                );
            }

            redirectAttributes.addFlashAttribute(
                    "message", "ดำเนินการกับการจอง #" + id + " เรียบร้อยแล้ว"
            );
        } catch (ResourceNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/admin/bookings";
    }

    private void requireStaff(Principal principal) {
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

        if (user.getRole() != UserRole.STAFF
                && user.getRole() != UserRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/{id}/payment/confirm")
    public String confirmPayment(
            @PathVariable("id") Long id,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        requireStaff(principal);

        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        try {
            paymentService.confirmPayment(id);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "บันทึกการชำระเงินและออกใบเสร็จเรียบร้อยแล้ว"
            );
        } catch (ResourceNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute(
                    "error", ex.getMessage()
            );
        }

        return "redirect:/admin/bookings";
    }
}