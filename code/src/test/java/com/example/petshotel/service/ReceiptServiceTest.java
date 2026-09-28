package com.example.petshotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.Receipt;
import com.example.petshotel.domain.enums.PaymentStatus;
import com.example.petshotel.repository.BookingRepository;
import com.example.petshotel.repository.ReceiptRepository;
import com.example.petshotel.service.impl.ReceiptServiceImpl;
import com.example.petshotel.exception.ResourceNotFoundException;

public class ReceiptServiceTest  {
    private ReceiptRepository receiptRepository;
    private BookingRepository bookingRepository;
    private ReceiptServiceImpl receiptService;

    @BeforeEach 
    void setUp(){
        receiptRepository = mock(ReceiptRepository.class);
        bookingRepository = mock(BookingRepository.class);

        receiptService = new ReceiptServiceImpl(receiptRepository, bookingRepository);

    }

    private Booking bookingWithPrices(){
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setRoomAmount(new BigDecimal("5000.00"));
        booking.setServiceAmount(new BigDecimal("200.00"));
        booking.setSurchargeAmount(new BigDecimal("900.00"));
        booking.setDiscountAmount(new BigDecimal("610.00"));
        booking.setTotalPrice(new BigDecimal("5490.00"));
        booking.setPaymentStatus(PaymentStatus.PAID);
        booking.setPaidAmount(new BigDecimal("5490.00"));
        booking.setPaidAt(LocalDateTime.of(2026, 10, 1, 10, 0));

        return booking;
    }

    @Test 
    void createsReceiptUsingSavedBookingPrices(){
        Booking booking = bookingWithPrices();

        when(bookingRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(booking));
        when(receiptRepository.findByBookingId(1L))
            .thenReturn(Optional.empty());
        when(receiptRepository.save(any(Receipt.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Receipt result = receiptService.createReceipt(1L);
        
        ArgumentCaptor<Receipt> captor = ArgumentCaptor.forClass(Receipt.class);
        verify(receiptRepository).save(captor.capture());

        Receipt saved = captor.getValue();

        assertSame(booking, saved.getBooking());
        assertEquals(new BigDecimal("5000.00"), saved.getRoomAmount());
        assertEquals(new BigDecimal("200.00"), saved.getServiceAmount());
        assertEquals(new BigDecimal("900.00"), saved.getSurchargeAmount());
        assertEquals(new BigDecimal("610.00"), saved.getDiscountAmount());
        assertEquals(new BigDecimal("5490.00"), saved.getTotalAmount());
        assertNotNull(saved.getIssuedAt());
        assertNotNull(saved.getReceiptNumber());
        assertTrue(saved.getReceiptNumber().startsWith("REC-"));
        assertSame(saved, result);

    }

    @Test 
    void returnsExistingReceiptWithoutSavingAgain(){
        Booking booking = bookingWithPrices();

        Receipt existing = new Receipt();
        existing.setId(10L);
        existing.setBooking(booking);

        when(bookingRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(booking));
        when(receiptRepository.findByBookingId(1L))
            .thenReturn(Optional.of(existing));
        
        Receipt result = receiptService.createReceipt(1L);
        
        assertSame(existing, result);
        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    @Test 
    void rejectsMissingBooking(){
        when(bookingRepository.findByIdForUpdate(99L))
            .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class, () -> receiptService.createReceipt(99L));
        
        assertEquals("Booking not found: 99", exception.getMessage());
        verify(receiptRepository, never()).save(any(Receipt.class));
    }

    @Test 
    void rejectsBookingWithIncompletePrices(){
        Booking booking = bookingWithPrices();
        booking.setDiscountAmount(null);

        when(bookingRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(booking));
        when(receiptRepository.findByBookingId(1L))
            .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> receiptService.createReceipt(1L));

        verify(receiptRepository, never()).save(any(Receipt.class));
        
    }

    @Test
    void rejectsUnpaidBooking() {
        Booking booking = bookingWithPrices();
        booking.setPaymentStatus(PaymentStatus.UNPAID);
        booking.setPaidAmount(null);
        booking.setPaidAt(null);

        when(bookingRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(booking));

        assertThrows(
                IllegalStateException.class,
                () -> receiptService.createReceipt(1L));

        verify(receiptRepository, never()).save(any(Receipt.class));
    }


}
