package com.example.petshotel.controller.api;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.request.CreateDailyCareReportRequest;
import com.example.petshotel.dto.request.UpdateDailyCareReportRequest;
import com.example.petshotel.dto.response.DailyCareReportResponse;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.DailyCareReportService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/care-reports")
public class DailyCareReportRestController {
    private  final DailyCareReportService reportService;
    private final CurrentUserService currentUserService;

    public DailyCareReportRestController(DailyCareReportService reportService,CurrentUserService currentUserService) {
        this.reportService = reportService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/booking-pets/{bookingPetId}")
    public ResponseEntity<DailyCareReportResponse> createReport(@PathVariable  Long bookingPetId,Principal principal,
            @Valid @RequestBody  CreateDailyCareReportRequest request){

                DailyCareReportResponse response = reportService.createReport(bookingPetId, currentUserId(principal), request);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }

    @GetMapping("/{reportId}")
    public DailyCareReportResponse getReportById(@PathVariable Long reportId,Principal principal){
        return reportService.getReportById(reportId, currentUserId(principal));
    }

    @GetMapping("/booking-pets/{bookingPetId}")
    public List<DailyCareReportResponse> getReportsByBookingPet(@PathVariable  Long bookingPetId,Principal principal){
        return  reportService.getReportsByBookingPet(bookingPetId, currentUserId(principal));
    }

    @PutMapping("/{reportId}")
    public  DailyCareReportResponse updateReport(@PathVariable  Long reportId,Principal principal,
            @Valid  @RequestBody UpdateDailyCareReportRequest request){
                return reportService.updateReport(reportId, currentUserId(principal), request);
            }

     private Long currentUserId(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User user;
        try {
            user = currentUserService.getByEmail(principal.getName());
        } catch (ResourceNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return user.getId();
    }
}

