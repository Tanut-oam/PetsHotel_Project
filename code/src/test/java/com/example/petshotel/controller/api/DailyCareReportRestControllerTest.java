package com.example.petshotel.controller.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.request.CreateDailyCareReportRequest;
import com.example.petshotel.dto.request.UpdateDailyCareReportRequest;
import com.example.petshotel.dto.response.DailyCareReportResponse;
import com.example.petshotel.exception.GlobalExceptionHandler;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.DailyCareReportService;

class DailyCareReportRestControllerTest {

    private DailyCareReportService reportService;
    private CurrentUserService currentUserService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reportService = mock(DailyCareReportService.class);
        currentUserService = mock(CurrentUserService.class);

        User staff = new User();
        staff.setId(5L);
        staff.setEmail("staff@example.com");
        staff.setActive(true);

        when(currentUserService.getByEmail("staff@example.com"))
                .thenReturn(staff);

        DailyCareReportRestController controller =
                new DailyCareReportRestController(
                        reportService, currentUserService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createReportShouldReturnCreatedReport() throws Exception {
        when(reportService.createReport(
                eq(21L),
                eq(5L),
                any(CreateDailyCareReportRequest.class)
        )).thenReturn(createReportResponse());

        mockMvc.perform(post("/api/care-reports/booking-pets/21")
                .principal(() -> "staff@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "reportDate": "2026-09-24",
                      "feedingMorning": "Finished",
                      "feedingEvening": "Half",
                      "walkingMinutes": 20,
                      "grooming": "Brushed",
                      "mood": "Happy",
                      "healthNote": "Normal",
                      "generalNote": "Likes playing"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.bookingPetId").value(21))
                .andExpect(jsonPath("$.recordedById").value(5))
                .andExpect(jsonPath("$.petName").value("Mochi"));

        verify(reportService).createReport(
                eq(21L),
                eq(5L),
                any(CreateDailyCareReportRequest.class)
        );
    }

    @Test
    void createReportShouldRejectMissingReportDate() throws Exception {
        mockMvc.perform(post("/api/care-reports/booking-pets/21")
                .principal(() -> "staff@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "feedingMorning": "Finished",
                      "walkingMinutes": 20
                    }
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reportService);
    }

    @Test
    void getReportByIdShouldReturnReport() throws Exception {
        when(reportService.getReportById(100L, 5L))
                .thenReturn(createReportResponse());

        mockMvc.perform(get("/api/care-reports/100")
                .principal(() -> "staff@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.petName").value("Mochi"));

        verify(reportService).getReportById(100L, 5L);
    }

    @Test
    void getReportsByBookingPetShouldReturnReportList() throws Exception {
        when(reportService.getReportsByBookingPet(21L, 5L))
                .thenReturn(List.of(createReportResponse()));

        mockMvc.perform(get("/api/care-reports/booking-pets/21")
                .principal(() -> "staff@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].bookingPetId").value(21));

        verify(reportService).getReportsByBookingPet(21L, 5L);
    }

    @Test
    void updateReportShouldPassCorrectIdsAndRequest() throws Exception {
        UpdateDailyCareReportRequest expectedRequest =
                new UpdateDailyCareReportRequest(
                        21L,
                        LocalDate.of(2026, 9, 24),
                        "Finished",
                        "Half",
                        20,
                        "Brushed",
                        "Happy",
                        "Normal",
                        "Likes playing"
                );

        when(reportService.updateReport(
                100L, 5L, expectedRequest))
                .thenReturn(createReportResponse());

        mockMvc.perform(put("/api/care-reports/100")
                .principal(() -> "staff@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "bookingPetId": 21,
                      "reportDate": "2026-09-24",
                      "feedingMorning": "Finished",
                      "feedingEvening": "Half",
                      "walkingMinutes": 20,
                      "grooming": "Brushed",
                      "mood": "Happy",
                      "healthNote": "Normal",
                      "generalNote": "Likes playing"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.bookingPetId").value(21));

        verify(reportService).updateReport(
                100L, 5L, expectedRequest);
    }

    @Test
    void getReportByIdShouldRejectMissingPrincipal() throws Exception {
        mockMvc.perform(get("/api/care-reports/100"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(currentUserService, reportService);
    }

    @Test
    void getReportByIdShouldRejectUnknownUser() throws Exception {
        when(currentUserService.getByEmail("staff@example.com"))
                .thenThrow(new ResourceNotFoundException(
                        "User", "staff@example.com"));

        mockMvc.perform(get("/api/care-reports/100")
                .principal(() -> "staff@example.com"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reportService);
    }

    @Test
    void getReportByIdShouldRejectInactiveUser() throws Exception {
        User inactiveStaff = new User();
        inactiveStaff.setId(5L);
        inactiveStaff.setActive(false);

        when(currentUserService.getByEmail("staff@example.com"))
                .thenReturn(inactiveStaff);

        mockMvc.perform(get("/api/care-reports/100")
                .principal(() -> "staff@example.com"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reportService);
    }

    @Test
    void updateReportShouldRejectNegativeWalkingMinutes()
            throws Exception {
        mockMvc.perform(put("/api/care-reports/100")
                .principal(() -> "staff@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "bookingPetId": 21,
                      "reportDate": "2026-09-24",
                      "feedingMorning": "Finished",
                      "walkingMinutes": -10
                    }
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reportService);
    }

    private DailyCareReportResponse createReportResponse() {
        return new DailyCareReportResponse(
                100L,
                21L,
                10L,
                7L,
                "Mochi",
                5L,
                LocalDate.of(2026, 9, 24),
                "Finished",
                "Half",
                20,
                "Brushed",
                "Happy",
                "Normal",
                "Likes playing",
                LocalDateTime.of(2026, 9, 24, 9, 0)
        );
    }
}