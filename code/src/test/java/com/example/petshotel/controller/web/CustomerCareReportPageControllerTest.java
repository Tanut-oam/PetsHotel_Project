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
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.response.DailyCareReportResponse;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.DailyCareReportService;

class CustomerCareReportPageControllerTest {

    private final DailyCareReportService reportService =
            mock(DailyCareReportService.class);
    private final CurrentUserService currentUserService =
            mock(CurrentUserService.class);

    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new CustomerCareReportPageController(
                reportService, currentUserService))
        .setViewResolvers(new InternalResourceViewResolver(
                "/test-views/", ".html"))
        .build();

    @Test
    void showReportsShouldLoadReportsForLoggedInOwner()
            throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(true);

        DailyCareReportResponse report =
                new DailyCareReportResponse(
                        1L, 2L, 3L, 4L, "Mochi", 5L,
                        LocalDate.of(2026, 9, 25),
                        "กินหมด", "กินหมด", 15,
                        "แปรงขน", "ร่าเริง", "ปกติ", null,
                        LocalDateTime.of(2026, 9, 25, 10, 0)
                );
        List<DailyCareReportResponse> reports = List.of(report);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);
        when(reportService.getReportsForOwner(7L))
                .thenReturn(reports);

        mockMvc.perform(get("/reports")
                .principal(() -> "owner@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports"))
                .andExpect(model().attribute("reports", reports));

        verify(reportService).getReportsForOwner(7L);
    }

    @Test
    void showReportsShouldRejectMissingLogin() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(currentUserService, reportService);
    }

    @Test
    void showReportsShouldRejectUnknownUser() throws Exception {
        when(currentUserService.getByEmail("owner@example.com"))
                .thenThrow(new ResourceNotFoundException(
                        "User", "owner@example.com"));

        mockMvc.perform(get("/reports")
                .principal(() -> "owner@example.com"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reportService);
    }

    @Test
    void showReportsShouldRejectInactiveUser() throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(false);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);

        mockMvc.perform(get("/reports")
                .principal(() -> "owner@example.com"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(reportService);
    }
}