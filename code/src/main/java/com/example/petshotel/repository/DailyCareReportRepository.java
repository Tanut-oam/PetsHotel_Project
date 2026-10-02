package com.example.petshotel.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import com.example.petshotel.domain.entity.DailyCareReport;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.petshotel.dto.response.ReportBookingSummary;

public interface DailyCareReportRepository extends JpaRepository<DailyCareReport, Long> {
    boolean existsByBookingPet_IdAndReportDate(Long bookingPetId,LocalDate reportDate);
    boolean existsByBookingPet_IdAndReportDateAndIdNot(Long bookingPetId,LocalDate reportDate,Long reportId);
    List<DailyCareReport> findByBookingPet_IdOrderByReportDateDesc(Long bookingPetId);

    @EntityGraph(attributePaths = {
    "bookingPet.pet",
    "bookingPet.booking.user",
    "recordedBy"
    })
    List<DailyCareReport> findAllByOrderByReportDateDescIdDesc();

    @EntityGraph(attributePaths = {
            "bookingPet.pet",
            "bookingPet.booking.user",
            "recordedBy"
    })
    @Query("""
            select report
            from DailyCareReport report
            join report.bookingPet bookingPet
            join bookingPet.booking booking
            where booking.user.id = :ownerId
            order by report.reportDate desc, report.id desc
            """)
    List<DailyCareReport> findAllForOwner(@Param("ownerId") Long ownerId);
    @Query(
    value = """
        select new com.example.petshotel.dto.response.ReportBookingSummary(
            booking.id, booking.checkInDate, booking.checkOutDate
        )
        from Booking booking
        where booking.user.id = :ownerId
          and exists (
              select report.id
              from DailyCareReport report
              where report.bookingPet.booking.id = booking.id
          )
        order by booking.checkInDate desc, booking.id desc
        """,
    countQuery = """
        select count(booking.id)
        from Booking booking
        where booking.user.id = :ownerId
          and exists (
              select report.id
              from DailyCareReport report
              where report.bookingPet.booking.id = booking.id
          )
        """
    )
    Page<ReportBookingSummary> findReportBookingsForOwner(
            @Param("ownerId") Long ownerId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
        "bookingPet.pet",
        "bookingPet.booking.user",
        "recordedBy"
    })
    @Query("""
        select report
        from DailyCareReport report
        join report.bookingPet bookingPet
        join bookingPet.booking booking
        where booking.user.id = :ownerId
        and booking.id in :bookingIds
        order by report.reportDate desc, report.id desc
        """)
    List<DailyCareReport> findAllForOwnerAndBookingIds(
            @Param("ownerId") Long ownerId,
            @Param("bookingIds") Collection<Long> bookingIds
    );
}