package com.example.petshotel.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.Receipt;
import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.PaymentStatus;
import com.example.petshotel.dto.response.PaymentResponse;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.repository.BookingRepository;
import com.example.petshotel.service.PaymentService;
import com.example.petshotel.service.ReceiptService;

@Service
public class PaymentServiceImpl implements PaymentService{
    private final BookingRepository bookingRepository;
    private final ReceiptService receiptService;

    public PaymentServiceImpl(BookingRepository bookingRepository,ReceiptService receiptService){
        this.bookingRepository = bookingRepository;
        this.receiptService = receiptService;
    }

    @Override
    @Transactional
    public PaymentResponse confirmPayment(Long bookingId) {
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking", bookingId));

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            Receipt receipt = receiptService.createReceipt(bookingId);

            return new PaymentResponse(
                    booking.getId(),
                    booking.getPaymentStatus(),
                    booking.getPaidAmount(),
                    booking.getPaidAt(),
                    receipt.getId(),
                    receipt.getReceiptNumber());
        }

        BookingStatus status = booking.getStatus();

        if (status != BookingStatus.CONFIRMED
                && status != BookingStatus.CHECKED_IN
                && status != BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException(
                    "Booking cannot receive payment in its current status");
        }

        if (booking.getTotalPrice() == null
                || booking.getTotalPrice().signum() < 0) {
            throw new IllegalStateException(
                    "Booking has an invalid total price");
        }

        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setPaidAmount(booking.getTotalPrice());
        booking.setPaidAt(LocalDateTime.now(ZoneId.of("Asia/Bangkok")));

        bookingRepository.save(booking);

        Receipt receipt = receiptService.createReceipt(bookingId);

        return new PaymentResponse(
                booking.getId(),
                booking.getPaymentStatus(),
                booking.getPaidAmount(),
                booking.getPaidAt(),
                receipt.getId(),
                receipt.getReceiptNumber());
    }

}
