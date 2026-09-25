package com.example.petshotel.notification;

import org.springframework.context.ApplicationEvent;

import com.example.petshotel.domain.entity.Booking;

public class BookingConfirmedEvent extends ApplicationEvent{
    private final Booking booking;

    public BookingConfirmedEvent(Object source,Booking booking){
        super(booking);
        this.booking = booking;
    }
    
    public Booking getBooking(){
        return booking;
    }
}
