package com.example.petshotel.mapper;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.dto.response.BookingResponse;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        List<Long> petIds = booking.getBookingPets()
                .stream()
                .map(bookingPet -> bookingPet.getPet().getId())
                .toList();

        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .roomId(booking.getRoom().getId())
                .petIds(petIds)
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .status(booking.getStatus())
                .totalPrice(booking.getTotalPrice())
                .paymentStatus(booking.getPaymentStatus())
                .build();
    }
}