package com.example.petshotel.controller.web;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.dto.request.CreateBookingRequest;
import com.example.petshotel.dto.response.BookingResponse;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.exception.RoomNotAvailableException;
import com.example.petshotel.repository.*;
import com.example.petshotel.service.BookingService;

import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.HashMap;

@Controller
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PetRepository petRepository;
    private final ExtraServiceRepository extraServiceRepository;
    private final PromotionRepository promotionRepository;
    private final Validator validator;

    @InitBinder("bookingForm")
    public void configureBinding(WebDataBinder binder) {
        // userId must come from the logged-in account, not the form.
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
            Principal principal,
            Model model) {

        User user = currentUser(principal);

        CreateBookingRequest form = new CreateBookingRequest();
        form.setRoomId(roomId);

        loadChoices(model, user, form);
        model.addAttribute("bookingForm", form);

        return "booking";
    }

    @PostMapping
    public String createBooking(
            @ModelAttribute("bookingForm") CreateBookingRequest form,
            BindingResult bindingResult,
            Principal principal,
            Model model) {

        User user = currentUser(principal);
        form.setUserId(user.getId());

        // Zero means the optional service was not selected.
        if (form.getExtraServiceQuantities() != null) {
            form.getExtraServiceQuantities().entrySet()
                    .removeIf(entry -> Integer.valueOf(0).equals(entry.getValue()));
        }

        // Validate after assigning the trusted userId.
        // Conversion errors, such as invalid dates, are already in BindingResult.
        if (!bindingResult.hasErrors()) {
            validator.validate(form).forEach(violation ->
                    bindingResult.rejectValue(
                            violation.getPropertyPath().toString(),
                            "invalid",
                            violation.getMessage()
                    )
            );
        }

        if (bindingResult.hasErrors()) {
            loadChoices(model, user, form);
            return "booking";
        }

        BookingResponse created;

        try {
            created = bookingService.createBooking(form);
        } catch (ResourceNotFoundException
                 | RoomNotAvailableException
                 | IllegalArgumentException
                 | IllegalStateException ex) {

            bindingResult.reject("booking.failed", ex.getMessage());
            loadChoices(model, user, form);
            return "booking";
        }

        return "redirect:/bookings/" + created.getId();
    }

    @GetMapping("/{id}")
    public String showBooking(
            @PathVariable("id") Long id,
            Principal principal,
            Model model) {

        User user = currentUser(principal);
        BookingResponse booking;

        try {
            booking = bookingService.getBookingById(id);
        } catch (ResourceNotFoundException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // This customer page only displays the customer's own booking.
        if (!user.getId().equals(booking.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("booking", booking);
        return "booking-detail";
    }

    private User currentUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return user;
    }

    private void loadChoices(
            Model model,
            User user,
            CreateBookingRequest form) {

        var services = extraServiceRepository.findByActiveTrue();

        if (form.getExtraServiceQuantities() == null) {
            form.setExtraServiceQuantities(new HashMap<>());
        }

        for (var service : services) {
            form.getExtraServiceQuantities().putIfAbsent(service.getId(), 0);
        }

        model.addAttribute("customer", user);
        model.addAttribute("rooms", roomRepository.findByStatus(RoomStatus.ACTIVE));
        model.addAttribute(
                "pets",
                petRepository.findByOwner_IdAndActiveTrue(user.getId())
        );
        model.addAttribute("extraServices", services);
        model.addAttribute("promotions", promotionRepository.findByActiveTrue());
    }
}