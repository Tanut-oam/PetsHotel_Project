package com.example.petshotel.state;

import com.example.petshotel.domain.entity.Booking;

public class CancelledState implements BookingState {

    @Override
    public void confirm(Booking booking) {
        throw new IllegalStateException(
                "Cancelled booking cannot be confirmed"
        );
    }

    @Override
    public void checkIn(Booking booking) {
        throw new IllegalStateException(
                "Cancelled booking cannot check in"
        );
    }

    @Override
    public void checkOut(Booking booking) {
        throw new IllegalStateException(
                "Cancelled booking cannot check out"
        );
    }

    @Override
    public void cancel(Booking booking) {
        throw new IllegalStateException(
                "Booking is already cancelled"
        );
    }
}