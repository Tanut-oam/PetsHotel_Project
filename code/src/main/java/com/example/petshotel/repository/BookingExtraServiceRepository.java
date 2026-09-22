package com.example.petshotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.petshotel.domain.entity.BookingExtraService;

 
public interface BookingExtraServiceRepository extends JpaRepository<BookingExtraService, Long>{
    List<BookingExtraService> findByBookingId(Long bookingId);
    
}
