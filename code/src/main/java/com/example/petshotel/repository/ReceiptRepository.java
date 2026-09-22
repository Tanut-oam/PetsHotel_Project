package com.example.petshotel.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.petshotel.domain.entity.Receipt;


public interface ReceiptRepository extends JpaRepository<Receipt, Long>{
    Optional<Receipt> findByBookingId(Long bookingId);
    Optional<Receipt> findByReceiptNumber(String receiptNumber);
    
}
