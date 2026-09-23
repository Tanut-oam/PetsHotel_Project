package com.example.petshotel.service;

import java.util.List;
import java.util.Optional;

import com.example.petshotel.domain.entity.Receipt;

public interface ReceiptService {
    Receipt createReceipt(Long bookingId);
    Optional<Receipt> getReceiptById(Long id);
    Optional<Receipt> getReceiptByBookingId(Long bookingId);
    List<Receipt> getAllReceipts();
} 
