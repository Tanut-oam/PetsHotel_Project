package com.example.petshotel.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.petshotel.domain.entity.DailyCareReport;

public interface DailyCareReportRepository extends JpaRepository<DailyCareReport, Long> {
    boolean existsByBookingPet_IdAndReportDate(Long bookingPetId,LocalDate reportDate);
    boolean existsByBookingPet_IdAndReportDateAndIdNot(Long bookingPetId,LocalDate reportDate,Long reportId);
    List<DailyCareReport> findByBookingPet_IdOrderByReportDateDesc(Long bookingPetId);
}