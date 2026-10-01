package com.example.petshotel.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.petshotel.domain.entity.BookingPet;
import com.example.petshotel.domain.enums.BookingStatus;

public interface BookingPetRepository
        extends JpaRepository<BookingPet, Long> {

    List<BookingPet> findByBookingId(Long bookingId);

    void deleteByBookingId(Long bookingId);

    @Query("""
            SELECT DISTINCT bp.pet.id
            FROM BookingPet bp
            JOIN bp.booking b
            WHERE bp.pet.id IN :petIds
              AND b.status IN :statuses
              AND b.checkInDate < :checkOut
              AND b.checkOutDate > :checkIn
            ORDER BY bp.pet.id
            """)
    List<Long> findOverlappingPetIds(
            @Param("petIds") Collection<Long> petIds,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("statuses")
            Collection<BookingStatus> statuses
    );

    @Query("""
            SELECT bp
            FROM BookingPet bp
            JOIN FETCH bp.booking b
            JOIN FETCH b.user
            JOIN FETCH bp.pet
            WHERE b.status IN :statuses
            ORDER BY b.checkInDate DESC, bp.id DESC
            """)
    List<BookingPet> findReportableBookingPets(
            @Param("statuses")
            Collection<BookingStatus> statuses
    );
}