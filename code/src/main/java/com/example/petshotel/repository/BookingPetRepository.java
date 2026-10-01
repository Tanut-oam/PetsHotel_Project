package com.example.petshotel.repository;

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

        @Query("""
            select bp
            from BookingPet bp
            join fetch bp.booking b
            join fetch b.user
            join fetch bp.pet
            where b.status in :statuses
            order by b.checkInDate desc, bp.id desc
            """)
    List<BookingPet> findReportableBookingPets(
            @Param("statuses") Collection<BookingStatus> statuses);
}