package com.example.petshotel.service;
import java.util.List;
import com.example.petshotel.dto.request.CreateDailyCareReportRequest;
import com.example.petshotel.dto.request.UpdateDailyCareReportRequest;
import com.example.petshotel.dto.response.DailyCareReportResponse;
import com.example.petshotel.dto.response.ReportableBookingPetResponse;

import java.time.LocalDate;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.petshotel.dto.response.ReportBookingSummary;

public interface DailyCareReportService {
    DailyCareReportResponse createReport(Long bookingPetId,Long currentUserId,CreateDailyCareReportRequest request);
    DailyCareReportResponse getReportById(Long reportId,Long currentUserId);
    List<DailyCareReportResponse> getReportsByBookingPet(Long bookingPetId, Long currentUserId);
    List<DailyCareReportResponse> getReportsForOwner(Long currentUserId);
    List<DailyCareReportResponse> getAllReportsForStaffAndAdmin(Long currentUserId);
    List<ReportableBookingPetResponse> getReportableBookingPetsForStaffAndAdmin(Long currentUserId);
    DailyCareReportResponse updateReport(Long reportId,Long currentUserId,UpdateDailyCareReportRequest request);
    Page<ReportBookingSummary> getReportBookingsForOwner(Long currentUserId, Pageable pageable);
    List<DailyCareReportResponse> getReportsForOwnerBookings(Long currentUserId, Collection<Long> bookingIds);
    Page<ReportBookingSummary> getReportBookingsForOwnerOnDate(Long currentUserId,LocalDate reportDate,Pageable pageable);
}
