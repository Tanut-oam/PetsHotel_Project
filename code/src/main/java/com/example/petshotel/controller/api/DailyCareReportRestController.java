package com.example.petshotel.controller.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.petshotel.dto.request.CreateDailyCareReportRequest;
import com.example.petshotel.dto.response.DailyCareReportResponse;
import com.example.petshotel.service.DailyCareReportService;
import com.example.petshotel.dto.request.UpdateDailyCareReportRequest;
import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/care-reports")
public class DailyCareReportRestController {
    private  final DailyCareReportService reportService;

    public DailyCareReportRestController(DailyCareReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/booking-pets/{bookingPetId}")
    public ResponseEntity<DailyCareReportResponse> createReport(@PathVariable  Long bookingPetId,@RequestAttribute("currentUserId") Long currentUserId,
            @Valid @RequestBody  CreateDailyCareReportRequest request){

                DailyCareReportResponse response = reportService.createReport(bookingPetId, currentUserId, request);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }

    @GetMapping("/{reportId}")
    public DailyCareReportResponse getReportById(@PathVariable Long reportId,@RequestAttribute("currentUserId")Long currentUserId){
        return reportService.getReportById(reportId, currentUserId);
    }

    @GetMapping("/booking-pets/{bookingPetId}")
    public List<DailyCareReportResponse> getReportsByBookingPet(@PathVariable  Long bookingPetId,@RequestAttribute("currentUserId") Long currentUserId){
        return  reportService.getReportsByBookingPet(bookingPetId, currentUserId);
    }

    @PutMapping("/{reportId}")
    public  DailyCareReportResponse updateReport(@PathVariable  Long reportId,@RequestAttribute("currentUserId") Long currentUserId,
            @Valid  @RequestBody UpdateDailyCareReportRequest request){
                return reportService.updateReport(reportId, currentUserId, request);
            }
}

