package com.example.petshotel.notification;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.repository.BookingRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.mail.MailException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingConfirmedListener {

    private final EmailNotificationService emailNotificationService;
    private final BookingRepository bookingRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(
            propagation = Propagation.REQUIRES_NEW,
            readOnly = true
    )
    public void handleBookingConfirmed(BookingConfirmedEvent event) {
        Long bookingId = event.getBooking().getId();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking", bookingId));

        try {
            emailNotificationService.sendBookingConfirmedEmail(booking);
        } catch (MailException ex) {
            log.error(
                    "Failed to send confirmation email for Booking #{}",
                    bookingId,
                    ex
            );
        }
    }
}