package com.example.petshotel.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.entity.Receipt;
import com.example.petshotel.dto.response.BookingPriceResponse;
import com.example.petshotel.pricing.PricingContext;
import com.example.petshotel.repository.BookingRepository;
import com.example.petshotel.repository.ReceiptRepository;
import com.example.petshotel.service.PricingService;
import com.example.petshotel.service.ReceiptService;
import com.example.petshotel.repository.BookingExtraServiceRepository;

@Service 
public class ReceiptServiceImpl implements ReceiptService{
    
    private final ReceiptRepository receiptRepository;
    private final BookingRepository bookingRepository;
    private final BookingExtraServiceRepository bookingExtraServiceRepository;
    private final PricingService pricingService;

    public ReceiptServiceImpl(
            ReceiptRepository receiptRepository,
            BookingRepository bookingRepository,
            BookingExtraServiceRepository bookingExtraServiceRepository,
            PricingService pricingService) {
        this.receiptRepository = receiptRepository;
        this.bookingRepository = bookingRepository;
        this.bookingExtraServiceRepository = bookingExtraServiceRepository;
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

        int petCount = booking.getBookingPets().size();
        if (petCount <= 0) {
            throw new IllegalArgumentException(
                "Booking must have at least one pet"
            );
        }

        PricingContext context = new PricingContext();
        context.setPetCount(petCount);
        context.setRoom(booking.getRoom());
        context.setCheckIn(booking.getCheckInDate());
        context.setCheckOut(booking.getCheckOutDate());
        context.setExtraServices(bookingExtraServiceRepository.findByBookingId(bookingId));

        BookingPriceResponse price = pricingService.calculate(context);

        Receipt receipt = new Receipt();
        receipt.setBooking(booking);
        receipt.setReceiptNumber("REC-" + UUID.randomUUID());
        receipt.setIssuedAt(LocalDateTime.now());
        receipt.setRoomAmount(price.basePrice());
        receipt.setServiceAmount(price.extraServicesPrice());
        receipt.setSurchargeAmount(price.holidaySurcharge());
        receipt.setDiscountAmount(price.discountAmount());
        receipt.setTotalAmount(price.totalPrice());

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
