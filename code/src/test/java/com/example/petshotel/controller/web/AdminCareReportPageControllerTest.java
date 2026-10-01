package com.example.petshotel.controller.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.request.CreateDailyCareReportRequest;
import com.example.petshotel.dto.response.DailyCareReportResponse;
import com.example.petshotel.dto.response.ReportableBookingPetResponse;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.DailyCareReportService;
import com.example.petshotel.domain.enums.PetType;

class AdminCareReportPageControllerTest {

    private final DailyCareReportService reportService =
            mock(DailyCareReportService.class);
    private final CurrentUserService currentUserService =
            mock(CurrentUserService.class);

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new AdminCareReportPageController(
                    reportService, currentUserService))
            .build();

    @Test
    void showReportsShouldLoadReportsAndBookingPetOptionsForLoggedInAdmin()
            throws Exception {
        User admin = new User();
        admin.setId(7L);
        admin.setActive(true);

        DailyCareReportResponse report = new DailyCareReportResponse(
                1L, 2L, 3L, 4L, "Mochi", 7L,
                LocalDate.of(2026, 9, 25),
                "กินหมด", "กินหมด", 15,
                "แปรงขน", "ร่าเริง", "ปกติ", null,
                LocalDateTime.of(2026, 9, 25, 10, 0)
        );
        List<DailyCareReportResponse> reports = List.of(report);

        ReportableBookingPetResponse option =
                new ReportableBookingPetResponse(
                        2L, 3L, "Mochi", "Test Owner",
                        LocalDate.of(2026, 9, 23),
                        LocalDate.of(2026, 9, 25),
                        PetType.DOG, "Pomeranian", 3, 15.0,
                        "MALE", "ไม่มี", "อาหารปกติ", "ไม่มี"
                );
        List<ReportableBookingPetResponse> options = List.of(option);

        when(currentUserService.getByEmail("admin@example.com"))
                .thenReturn(admin);
        when(reportService.getAllReportsForStaffAndAdmin(7L))
                .thenReturn(reports);
        when(reportService.getReportableBookingPetsForStaffAndAdmin(7L))
                .thenReturn(options);

        mockMvc.perform(get("/admin/reports")
                .principal(() -> "admin@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reports"))
                .andExpect(model().attribute("reports", reports))
                .andExpect(model().attribute("bookingPetOptions", options));

        verify(reportService).getAllReportsForStaffAndAdmin(7L);
        verify(reportService).getReportableBookingPetsForStaffAndAdmin(7L);
    }

    @Test
    void showReportsShouldRejectMissingLogin() throws Exception {
        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(currentUserService, reportService);
    }

    @Test
    void showReportsShouldRejectUnknownUser() throws Exception {
        when(currentUserService.getByEmail("admin@example.com"))
                .thenThrow(new ResourceNotFoundException(
                        "User", "admin@example.com"));

        mockMvc.perform(get("/admin/reports")
                .principal(() -> "admin@example.com"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reportService);
    }

    @Test
    void showReportsShouldRejectInactiveUser() throws Exception {
        User admin = new User();
        admin.setId(7L);
        admin.setActive(false);

        when(currentUserService.getByEmail("admin@example.com"))
                .thenReturn(admin);

        mockMvc.perform(get("/admin/reports")
                .principal(() -> "admin@example.com"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reportService);
    }

    @Test
    void createReportShouldUseLoggedInAdminAndSubmittedData()
            throws Exception {
        User admin = new User();
        admin.setId(7L);
        admin.setActive(true);

        when(currentUserService.getByEmail("admin@example.com"))
                .thenReturn(admin);

        mockMvc.perform(post("/admin/reports")
                .principal(() -> "admin@example.com")
                .param("bookingPetId", "2")
                .param("reportDate", "2026-09-25")
                .param("feedingMorning", "กินหมด")
                .param("walkingMinutes", "15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reports/pets/2"))
                .andExpect(flash().attribute(
                        "message", "บันทึกรายงานการดูแลสำเร็จ"));

        ArgumentCaptor<CreateDailyCareReportRequest> requestCaptor =
                ArgumentCaptor.forClass(CreateDailyCareReportRequest.class);

        verify(reportService).createReport(
                eq(2L), eq(7L), requestCaptor.capture());

        assertEquals(
                LocalDate.of(2026, 9, 25),
                requestCaptor.getValue().reportDate());
        assertEquals(
                "กินหมด",
                requestCaptor.getValue().feedingMorning());
        assertEquals(
                Integer.valueOf(15),
                requestCaptor.getValue().walkingMinutes());
    }

    @Test
    void createReportShouldRejectMissingBookingPet() throws Exception {
        User admin = new User();
        admin.setId(7L);
        admin.setActive(true);

        when(currentUserService.getByEmail("admin@example.com"))
                .thenReturn(admin);

        mockMvc.perform(post("/admin/reports")
                .principal(() -> "admin@example.com")
                .param("reportDate", "2026-09-25"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reports"))
                .andExpect(flash().attribute(
                        "error",
                        "กรุณาตรวจสอบสัตว์ในการจองและข้อมูลรายงาน"));

        verifyNoInteractions(reportService);
    }

    @Test
    void createReportShouldRejectInvalidWalkingMinutes()
            throws Exception {
        User admin = new User();
        admin.setId(7L);
        admin.setActive(true);

        when(currentUserService.getByEmail("admin@example.com"))
                .thenReturn(admin);

        mockMvc.perform(post("/admin/reports")
                .principal(() -> "admin@example.com")
                .param("bookingPetId", "2")
                .param("reportDate", "2026-09-25")
                .param("walkingMinutes", "-1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reports/pets/2"))
                .andExpect(flash().attribute(
                        "error",
                        "กรุณาตรวจสอบสัตว์ในการจองและข้อมูลรายงาน"));

        verifyNoInteractions(reportService);
    }

    @Test
    void createReportShouldShowErrorWhenServiceRejectsReport()
            throws Exception {
        User admin = new User();
        admin.setId(7L);
        admin.setActive(true);

        when(currentUserService.getByEmail("admin@example.com"))
                .thenReturn(admin);
        when(reportService.createReport(
                eq(2L),
                eq(7L),
                any(CreateDailyCareReportRequest.class)))
                .thenThrow(new IllegalStateException(
                        "Report already exists"));

        mockMvc.perform(post("/admin/reports")
                .principal(() -> "admin@example.com")
                .param("bookingPetId", "2")
                .param("reportDate", "2026-09-25"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/reports/pets/2"))
                .andExpect(flash().attribute(
                        "error",
                        "ไม่สามารถบันทึกรายงานได้ กรุณาตรวจสอบวันที่ การจอง และรายงานที่มีอยู่แล้ว"));
    }

    @Test
    void createReportShouldRejectMissingLogin() throws Exception {
        mockMvc.perform(post("/admin/reports")
                .param("bookingPetId", "2")
                .param("reportDate", "2026-09-25"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(currentUserService, reportService);
    }
}