package com.example.petshotel.state;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.enums.BookingStatus;

public class ConfirmedState implements BookingState {

    @Override
    public void confirm(Booking booking) {
        throw new IllegalStateException(
                "Booking is already confirmed"
        );
    }

    @Override
    public void checkIn(Booking booking) {
        booking.setStatus(BookingStatus.CHECKED_IN);
    }

    @Override
    public void checkOut(Booking booking) {
        throw new IllegalStateException(
                "Booking must check in before check out"
        );
    }

    @Override
    public void cancel(Booking booking) {
        booking.setStatus(BookingStatus.CANCELLED);
    }
}