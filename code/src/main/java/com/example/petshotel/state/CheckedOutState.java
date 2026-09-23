package com.example.petshotel.state;

import com.example.petshotel.domain.entity.Booking;

public class CheckedOutState implements BookingState {

    @Override
    public void confirm(Booking booking) {
        throw new IllegalStateException(
                "Checked-out booking cannot change state"
        );
    }

    @Override
    public void checkIn(Booking booking) {
        throw new IllegalStateException(
                "Checked-out booking cannot check in"
        );
    }

    @Override
    public void checkOut(Booking booking) {
        throw new IllegalStateException(
                "Booking is already checked out"
        );
    }

    @Override
    public void cancel(Booking booking) {
        throw new IllegalStateException(
                "Checked-out booking cannot be cancelled"
        );
    }
}