package com.example.petshotel.state;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.enums.BookingStatus;

public class PendingState implements BookingState {

    @Override
    public void confirm(Booking booking) {
        booking.setStatus(BookingStatus.CONFIRMED);
    }

    @Override
    public void checkIn(Booking booking) {
        throw new IllegalStateException(
                "Pending booking cannot check in"
        );
    }

    @Override
    public void checkOut(Booking booking) {
        throw new IllegalStateException(
                "Pending booking cannot check out"
        );
    }

    @Override
    public void cancel(Booking booking) {
        booking.setStatus(BookingStatus.CANCELLED);
    }
}