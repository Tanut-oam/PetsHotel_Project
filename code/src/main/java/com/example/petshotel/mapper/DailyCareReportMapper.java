package com.example.petshotel.mapper;

import org.springframework.stereotype.Component;

import com.example.petshotel.domain.entity.DailyCareReport;
import com.example.petshotel.dto.response.DailyCareReportResponse;

@Component 
public class DailyCareReportMapper{
    public DailyCareReportResponse toResponse(DailyCareReport report){
        return new DailyCareReportResponse(
            report.getId(),
            report.getBookingPet().getId(),
            report.getBookingPet().getBooking().getId(),
            report.getBookingPet().getPet().getId(),
            report.getBookingPet().getPet().getName(),
            report.getRecordedBy().getId(),
            report.getReportDate(),
            report.getFeedingMorning(),
            report.getFeedingEvening(),
            report.getWalkingMinutes(),
            report.getGrooming(),
            report.getMood(),
            report.getHealthNote(),
            report.getGeneralNote(),
            report.getCreatedAt()
        );
    }
}
