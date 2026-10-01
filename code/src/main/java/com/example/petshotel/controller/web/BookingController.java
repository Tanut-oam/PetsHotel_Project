package com.example.petshotel.controller.web;

import java.security.Principal;
import java.util.HashMap;
import java.util.Objects;
import java.time.LocalDate;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.request.CreateBookingRequest;
import com.example.petshotel.dto.response.BookingResponse;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.exception.RoomNotAvailableException;
import com.example.petshotel.service.BookingService;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.ExtraServiceService;
import com.example.petshotel.service.PetService;
import com.example.petshotel.service.PromotionService;
import com.example.petshotel.service.RoomService;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final CurrentUserService currentUserService;
    private final RoomService roomService;
    private final PetService petService;
    private final ExtraServiceService extraServiceService;
    private final PromotionService promotionService;
    private final Validator validator;

    @InitBinder("bookingForm")
    public void configureBinding(WebDataBinder binder) {
        // userId ต้องมาจากบัญชีที่ล็อกอิน ไม่รับจาก form
        binder.setAllowedFields(
                "roomId",
                "petIds",
                "checkInDate",
                "checkOutDate",
                "promotionId",
                "extraServiceQuantities[*]"
        );
    }

    @GetMapping("/new")
    public String showForm(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkIn,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkOut,
            Principal principal,
            Model model) {

        User user = currentUser(principal);

        CreateBookingRequest form = new CreateBookingRequest();
        form.setRoomId(roomId);
        form.setCheckInDate(checkIn);
        form.setCheckOutDate(checkOut);

        loadChoices(model, user, form);
        model.addAttribute("bookingForm", form);

        return "booking";
    }

    @PostMapping
    public String createBooking(
            @ModelAttribute("bookingForm")
            CreateBookingRequest form,
            BindingResult bindingResult,
            Principal principal,
            Model model) {

        User user = currentUser(principal);

        // ป้องกันการจองในชื่อผู้ใช้อื่น
        form.setUserId(user.getId());

        removeUnselectedServices(form);

        validateForm(form, bindingResult);

        if (bindingResult.hasErrors()) {
            loadChoices(model, user, form);
            return "booking";
        }

        try {
            BookingResponse created =
                    bookingService.createBooking(form);

            return "redirect:/bookings/"
                    + created.getId();

        } catch (ResourceNotFoundException
                 | RoomNotAvailableException
                 | IllegalArgumentException
                 | IllegalStateException ex) {

            bindingResult.reject(
                    "booking.failed",
                    ex.getMessage()
            );

            loadChoices(model, user, form);
            return "booking";
        }
    }

    @GetMapping
    public String showMyBookings(
            Principal principal,
            Model model) {

        User user = currentUser(principal);

        model.addAttribute(
                "bookings",
                bookingService.getBookingsByUserId(
                        user.getId()
                )
        );

        return "my-bookings";
    }

    @GetMapping("/{id}")
    public String showBooking(
            @PathVariable("id") Long id,
            Principal principal,
            Model model) {

        User user = currentUser(principal);
        BookingResponse booking = findBooking(id);

        requireOwner(user, booking);

        model.addAttribute("booking", booking);

        return "booking-detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(
            @PathVariable("id") Long id,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User user = currentUser(principal);
        BookingResponse booking = findBooking(id);

        requireOwner(user, booking);

        try {
            bookingService.cancelBooking(id);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "ยกเลิกการจองเรียบร้อยแล้ว"
            );

        } catch (ResourceNotFoundException ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND
            );

        } catch (IllegalArgumentException
                 | IllegalStateException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/bookings/" + id;
    }

    private User currentUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED
            );
        }

        User user;

        try {
            user = currentUserService.getByEmail(
                    principal.getName()
            );
        } catch (ResourceNotFoundException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED
            );
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN
            );
        }

        return user;
    }

    private BookingResponse findBooking(Long id) {
        try {
            return bookingService.getBookingById(id);
        } catch (ResourceNotFoundException ex) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND
            );
        }
    }

    private void requireOwner(
            User user,
            BookingResponse booking) {

        if (!Objects.equals(
                user.getId(),
                booking.getUserId())) {

            // ใช้ 404 เพื่อไม่เปิดเผยรายการของผู้ใช้อื่น
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND
            );
        }
    }

    private void removeUnselectedServices(
            CreateBookingRequest form) {

        if (form.getExtraServiceQuantities() == null) {
            return;
        }

        form.getExtraServiceQuantities()
                .entrySet()
                .removeIf(entry ->
                        Integer.valueOf(0)
                                .equals(entry.getValue())
                );
    }

    private void validateForm(
            CreateBookingRequest form,
            BindingResult bindingResult) {

        // Conversion error เช่นรูปแบบวันที่ผิด
        // จะอยู่ใน BindingResult อยู่แล้ว
        if (bindingResult.hasErrors()) {
            return;
        }

        validator.validate(form).forEach(violation ->
                bindingResult.rejectValue(
                        violation.getPropertyPath()
                                .toString(),
                        "invalid",
                        violation.getMessage()
                )
        );
    }

    private void loadChoices(
            Model model,
            User user,
            CreateBookingRequest form) {

        var services =
                extraServiceService
                        .getActiveExtraServices();

        if (form.getExtraServiceQuantities() == null) {
            form.setExtraServiceQuantities(
                    new HashMap<>()
            );
        }

        for (var service : services) {
            form.getExtraServiceQuantities()
                    .putIfAbsent(
                            service.getId(),
                            0
                    );
        }

        model.addAttribute("customer", user);

        model.addAttribute(
                "rooms",
                roomService.getActiveRooms()
        );

        model.addAttribute(
                "pets",
                petService.getPetsByOwner(
                        user.getId()
                )
        );

        model.addAttribute(
                "extraServices",
                services
        );

        model.addAttribute(
                "promotions",
                promotionService
                        .getActivePromotions()
        );
    }
}