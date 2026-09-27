package com.example.petshotel.repository;

import com.example.petshotel.domain.entity.Booking;
import com.example.petshotel.domain.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByRoomId(Long roomId);

        @Query("""
            SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
            FROM Booking b
            WHERE b.room.id = :roomId
              AND b.status IN :statuses
              AND b.checkInDate < :checkOut
              AND b.checkOutDate > :checkIn
            """)
    boolean existsOverlappingBooking(@Param("roomId") Long roomId,
                                     @Param("checkIn") LocalDate checkIn,
                                     @Param("checkOut") LocalDate checkOut,
                                     @Param("statuses") Collection<BookingStatus> statuses);
                                     
}