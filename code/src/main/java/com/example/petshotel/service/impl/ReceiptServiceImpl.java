package com.example.petshotel.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.Receipt;
import com.example.petshotel.pricing.PricingContext;
import com.example.petshotel.repository.BookingRepository;
import com.example.petshotel.repository.ReceiptRepository;
import com.example.petshotel.service.PricingService;
import com.example.petshotel.service.ReceiptService;

@Service 
public class ReceiptServiceImpl implements ReceiptService{
    
    private final ReceiptRepository receiptRepository;
    private final BookingRepository bookingRepository;
    private final PricingService pricingService;

    public ReceiptServiceImpl(
            ReceiptRepository receiptRepository,
            BookingRepository bookingRepository,
            PricingService pricingService) {
        this.receiptRepository = receiptRepository;
        this.bookingRepository = bookingRepository;
        this.pricingService = pricingService;
    }

    @Override 
    @Transactional 
    public Receipt createReceipt(Long bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

        Optional<Receipt> existingReceipt = receiptRepository.findByBookingId(bookingId);
            if (existingReceipt.isPresent()) {
            return existingReceipt.get();
        }

        PricingContext context = new PricingContext();
        context.setRoom(booking.getRoom());
        context.setCheckIn(booking.getCheckInDate());
        context.setCheckOut(booking.getCheckOutDate());

        var totalAmount = pricingService.calculateTotalPrice(context);

        Receipt receipt = new Receipt();
        receipt.setBooking(booking);
        receipt.setReceiptNumber("REC-"+ System.currentTimeMillis());
        receipt.setTotalAmount(totalAmount);
        receipt.setIssuedAt(LocalDateTime.now());

        return receiptRepository.save(receipt);

    }

    @Override 
    @Transactional(readOnly = true)
    public Optional<Receipt> getReceiptById(Long id){
        return receiptRepository.findById(id);
    }

    @Override 
    @Transactional(readOnly = true)
    public Optional<Receipt> getReceiptByBookingId(Long bookingId){
        return receiptRepository.findByBookingId(bookingId);
    }

    @Override 
    @Transactional(readOnly = true)
    public List<Receipt> getAllReceipts(){
        return receiptRepository.findAll();
    }

}
