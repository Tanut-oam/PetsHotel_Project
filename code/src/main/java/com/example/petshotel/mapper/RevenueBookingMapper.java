package com.example.petshotel.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.BookingPet;
import com.example.petshotel.domain.entity.Receipt;
import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.response.RevenueBookingResponse;

@Component
public class RevenueBookingMapper {

    public RevenueBookingResponse toResponse(Receipt receipt) {
        Booking booking = receipt.getBooking();

        String customerName = "ไม่ระบุ";
        User customer = booking.getUser();

        if (customer != null) {
            String firstName =
                    Objects.toString(customer.getFirstName(), "");
            String lastName =
                    Objects.toString(customer.getLastName(), "");

            String fullName = (firstName + " " + lastName).trim();

            if (!fullName.isEmpty()) {
                customerName = fullName;
            }
        }

        List<String> petNames = new ArrayList<>();

        for (BookingPet bookingPet : booking.getBookingPets()) {
            if (bookingPet.getPet() != null
                    && bookingPet.getPet().getName() != null) {
                petNames.add(bookingPet.getPet().getName());
            }
        }

        String roomNumber = "ไม่ระบุ";

        if (booking.getRoom() != null
                && booking.getRoom().getRoomNumber() != null) {
            roomNumber = booking.getRoom().getRoomNumber();
        }

        return new RevenueBookingResponse(
                booking.getId(),
                customerName,
                List.copyOf(petNames),
                roomNumber,
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getPaidAt(),
                receipt.getReceiptNumber(),
                receipt.getRoomAmount(),
                receipt.getServiceAmount(),
                receipt.getSurchargeAmount(),
                receipt.getDiscountAmount(),
                receipt.getTotalAmount()
        );
    }
}