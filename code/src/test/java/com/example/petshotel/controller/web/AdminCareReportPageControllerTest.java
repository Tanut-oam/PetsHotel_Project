package com.example.petshotel.controller.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.response.DailyCareReportResponse;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.DailyCareReportService;

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
    void showReportsShouldLoadReportsForLoggedInAdmin() throws Exception {
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

        when(currentUserService.getByEmail("admin@example.com"))
                .thenReturn(admin);
        when(reportService.getAllReportsForStaffAndAdmin(7L))
                .thenReturn(reports);

        mockMvc.perform(get("/admin/reports")
                .principal(() -> "admin@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reports"))
                .andExpect(model().attribute("reports", reports));

        verify(reportService).getAllReportsForStaffAndAdmin(7L);
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
}