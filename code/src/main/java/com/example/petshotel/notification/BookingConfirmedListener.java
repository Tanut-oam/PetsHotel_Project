package com.example.petshotel.notification;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;


@Component
@RequiredArgsConstructor
public class BookingConfirmedListener {
    private final EmailNotificationService emailNotificationService;

    @Async
    @EventListener
    public void handleBookingConfirmed(BookingConfirmedEvent event){
        emailNotificationService.sendBookingConfirmedEmail(event.getBooking());
    }
}
