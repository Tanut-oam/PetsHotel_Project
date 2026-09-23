package com.example.petshotel.state;

import com.example.petshotel.domain.entity.Booking;

public interface BookingState {

    void confirm(Booking booking);

    void checkIn(Booking booking);

    void checkOut(Booking booking);

    void cancel(Booking booking);
}