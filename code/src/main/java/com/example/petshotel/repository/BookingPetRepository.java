package com.example.petshotel.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.petshotel.domain.entity.BookingPet;
import com.example.petshotel.domain.enums.BookingStatus;

public interface BookingPetRepository extends JpaRepository<BookingPet, Long> {

    List<BookingPet> findByBookingId(Long bookingId);

    void deleteByBookingId(Long bookingId);

    // นับจำนวนสัตว์ในห้อง ในช่วงวันจองกับช่วงวันที่ขอ
    @Query("""
            SELECT COUNT(bp) FROM BookingPet bp
            WHERE bp.booking.room.id = :roomId
              AND bp.booking.status IN :statuses
              AND bp.booking.checkInDate < :checkOut
              AND bp.booking.checkOutDate > :checkIn
            """)
    long countPetsInOverlappingBookings(@Param("roomId") Long roomId,
                                        @Param("checkIn") LocalDate checkIn,
                                        @Param("checkOut") LocalDate checkOut,
                                        @Param("statuses") Collection<BookingStatus> statuses);
}