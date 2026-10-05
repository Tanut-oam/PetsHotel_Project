package com.example.petshotel.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.Receipt;
import com.example.petshotel.domain.enums.PaymentStatus;
import com.example.petshotel.domain.entity.User;
import com.example.petshotel.repository.BookingRepository;
import com.example.petshotel.repository.ReceiptRepository;
import com.example.petshotel.service.ReceiptService;
import com.example.petshotel.exception.ResourceNotFoundException;

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
    public Receipt createReceipt(Long bookingId) {
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Booking", bookingId));

        if (booking.getPaymentStatus() != PaymentStatus.PAID) {
            throw new IllegalStateException(
                    "Payment must be confirmed before issuing a receipt");
        }

        if (booking.getPaidAt() == null
                || booking.getPaidAmount() == null
                || booking.getTotalPrice() == null
                || booking.getPaidAmount().signum() < 0
                || booking.getPaidAmount()
                        .compareTo(booking.getTotalPrice()) != 0) {
            throw new IllegalStateException(
                    "Booking has incomplete or inconsistent payment data");
        }

        Optional<Receipt> existingReceipt =
                receiptRepository.findByBookingId(bookingId);

        if (existingReceipt.isPresent()) {
            return existingReceipt.get();
        }

        if (booking.getRoomAmount() == null
                || booking.getServiceAmount() == null
                || booking.getSurchargeAmount() == null
                || booking.getDiscountAmount() == null) {
            throw new IllegalStateException(
                    "Booking has no complete price snapshot: " + bookingId);
        }

        User customer = booking.getUser();
        String customerName =
                (customer.getFirstName().trim()
                        + " "
                        + customer.getLastName().trim()).trim();

        Receipt receipt = new Receipt();
        receipt.setBooking(booking);
        receipt.setCustomerName(customerName);
        receipt.setReceiptNumber("REC-" + UUID.randomUUID());
        receipt.setIssuedAt(LocalDateTime.now(ZoneId.of("Asia/Bangkok")));
        receipt.setRoomAmount(booking.getRoomAmount());
        receipt.setServiceAmount(booking.getServiceAmount());
        receipt.setSurchargeAmount(booking.getSurchargeAmount());
        receipt.setDiscountAmount(booking.getDiscountAmount());
        receipt.setTotalAmount(booking.getPaidAmount());

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
