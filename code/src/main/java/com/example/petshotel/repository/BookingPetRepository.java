package com.example.petshotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.petshotel.domain.entity.BookingPet;

public interface BookingPetRepository extends JpaRepository<BookingPet, Long> {

    List<BookingPet> findByBookingId(Long bookingId);

    void deleteByBookingId(Long bookingId);
}