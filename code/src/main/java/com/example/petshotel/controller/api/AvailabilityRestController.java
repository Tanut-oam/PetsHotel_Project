package com.example.petshotel.controller.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.dto.request.AvailabilitySearchRequest;
import com.example.petshotel.dto.response.AvailabilityResponse;
import com.example.petshotel.dto.response.RoomAvailabilityCalendarResponse;
import com.example.petshotel.mapper.AvailabilityMapper;
import com.example.petshotel.service.AvailabilityService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rooms")
public class AvailabilityRestController {

    private final AvailabilityService availabilityService;
    private final AvailabilityMapper availabilityMapper;

    public AvailabilityRestController(
            AvailabilityService availabilityService,
            AvailabilityMapper availabilityMapper) {

        this.availabilityService = availabilityService;
        this.availabilityMapper = availabilityMapper;
    }

    @GetMapping("/available")
    public AvailabilityResponse findAvailableRooms(
            @Valid
            @ModelAttribute
            AvailabilitySearchRequest request) {

        List<Room> rooms =
                availabilityService.findAvailableRooms(
                        request.checkIn(),
                        request.checkOut(),
                        request.petCount()
                );

        return availabilityMapper.toResponse(
                request,
                rooms
        );
    }

    @GetMapping("/{roomId}/availability-calendar")
    public RoomAvailabilityCalendarResponse
            getAvailabilityCalendar(
                    @PathVariable Long roomId,

                    @RequestParam
                    @DateTimeFormat(
                            iso = DateTimeFormat.ISO.DATE
                    )
                    LocalDate fromDate,

                    @RequestParam
                    @DateTimeFormat(
                            iso = DateTimeFormat.ISO.DATE
                    )
                    LocalDate toDate) {

        return availabilityService
                .getRoomAvailabilityCalendar(
                        roomId,
                        fromDate,
                        toDate
                );
    }
}