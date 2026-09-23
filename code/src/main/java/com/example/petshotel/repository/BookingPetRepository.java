package com.example.petshotel.repository;

import com.example.petshotel.domain.entity.BookingPet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingPetRepository extends JpaRepository<BookingPet, Long> {

    List<BookingPet> findByBookingId(Long bookingId);

    void deleteByBookingId(Long bookingId);
}