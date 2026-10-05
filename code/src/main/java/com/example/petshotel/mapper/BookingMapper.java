package com.example.petshotel.mapper;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.dto.response.BookingResponse;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {

        LocalDate currentDate = LocalDate.now(
                ZoneId.of("Asia/Bangkok")
        );

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
                .checkInAllowed(
                        booking.canCheckInOn(currentDate)
                )
                .checkOutAllowed(
                        booking.canCheckOutOn(currentDate)
                )
                .roomName(booking.getRoom().getName())
                .petNames(
                        booking.getBookingPets().stream()
                                .map(item -> item.getPet().getName())
                                .toList()
                )
                .promotionName(booking.getPromotionName())
                .roomAmount(booking.getRoomAmount())
                .serviceAmount(booking.getServiceAmount())
                .surchargeAmount(booking.getSurchargeAmount())
                .discountAmount(booking.getDiscountAmount())
                .extraServices(
                        booking.getExtraServices().stream()
                                .map(item -> new BookingResponse.ExtraServiceLine(
                                        item.getExtraService().getName(),
                                        item.getQuantity(),
                                        item.getUnitPrice(),
                                        item.getTotalPrice(),
                                        item.getBookingPet() != null
                                                ? item.getBookingPet().getPet().getId() : null,
                                        item.getBookingPet() != null
                                                ? item.getBookingPet().getPet().getName() : null
                                ))
                                .toList()
                )
                .build();
    }
}
