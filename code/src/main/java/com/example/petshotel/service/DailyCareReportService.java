package com.example.petshotel.service;
import java.util.List;
import com.example.petshotel.dto.request.CreateDailyCareReportRequest;
import com.example.petshotel.dto.request.UpdateDailyCareReportRequest;
import com.example.petshotel.dto.response.DailyCareReportResponse;

public interface DailyCareReportService {
    DailyCareReportResponse createReport(Long bookingPetId,Long currentUserId,CreateDailyCareReportRequest request);
    DailyCareReportResponse getReportById(Long reportId,Long currentUserId);
    List<DailyCareReportResponse> getReportsByBookingPet(Long bookingPetId, Long currentUserId);
    DailyCareReportResponse updateReport(Long reportId,Long currentUserId,UpdateDailyCareReportRequest request);
}
