package com.example.petshotel.state;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.enums.BookingStatus;

public class CheckedInState implements BookingState {

    @Override
    public void confirm(Booking booking) {
        throw new IllegalStateException(
                "Checked-in booking cannot be confirmed again"
        );
    }

    @Override
    public void checkIn(Booking booking) {
        throw new IllegalStateException(
                "Booking is already checked in"
        );
    }

    @Override
    public void checkOut(Booking booking) {
        booking.setStatus(BookingStatus.CHECKED_OUT);
    }

    @Override
    public void cancel(Booking booking) {
        throw new IllegalStateException(
                "Checked-in booking cannot be cancelled"
        );
    }
}