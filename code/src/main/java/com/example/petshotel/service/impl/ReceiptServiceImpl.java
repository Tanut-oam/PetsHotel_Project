package com.example.petshotel.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.Receipt;
import com.example.petshotel.repository.BookingRepository;
import com.example.petshotel.repository.ReceiptRepository;
import com.example.petshotel.service.ReceiptService;

@Service 
public class ReceiptServiceImpl implements ReceiptService{
    
    private final ReceiptRepository receiptRepository;
    private final BookingRepository bookingRepository;

    public ReceiptServiceImpl(
            ReceiptRepository receiptRepository,
            BookingRepository bookingRepository) {
        this.receiptRepository = receiptRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override 
    @Transactional 
    public Receipt createReceipt(Long bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        Optional<Receipt> existingReceipt = receiptRepository.findByBookingId(bookingId);
            if (existingReceipt.isPresent()) {
            return existingReceipt.get();
        }

        if (booking.getRoomAmount() == null
                || booking.getServiceAmount() == null
                || booking.getSurchargeAmount() == null
                || booking.getDiscountAmount() == null
                || booking.getTotalPrice() == null) {
            throw new IllegalStateException(
                    "Booking has no complete price snapshot: " + bookingId);
        }

        Receipt receipt = new Receipt();
        receipt.setBooking(booking);
        receipt.setReceiptNumber("REC-" + UUID.randomUUID());
        receipt.setIssuedAt(LocalDateTime.now());
        receipt.setRoomAmount(booking.getRoomAmount());
        receipt.setServiceAmount(booking.getServiceAmount());
        receipt.setSurchargeAmount(booking.getSurchargeAmount());
        receipt.setDiscountAmount(booking.getDiscountAmount());
        receipt.setTotalAmount(booking.getTotalPrice());

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
