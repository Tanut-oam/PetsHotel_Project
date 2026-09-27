package com.example.petshotel.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.dto.response.RecentBookingResponse;

@Component 
public class RecentBookingMapper {
    public RecentBookingResponse toResponse(Booking booking){
        String customerName = booking.getUser().getFirstName() + " " + booking.getUser().getLastName();

        List<String> petName = booking.getBookingPets().stream()
            .map(bookingPet -> bookingPet.getPet().getName()).toList();

        return new RecentBookingResponse(
            booking.getId(),
            customerName,
            petName,
            booking.getRoom().getRoomNumber(),
            booking.getStatus()
        );
    }
}
