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
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.response.DailyCareReportResponse;
import com.example.petshotel.dto.response.ReportBookingSummary;
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

        ReportBookingSummary booking = new ReportBookingSummary(
                3L,
                LocalDate.of(2026, 9, 25),
                LocalDate.of(2026, 9, 27)
        );

        DailyCareReportResponse report =
                new DailyCareReportResponse(
                        1L, 2L, 3L, 4L, "Mochi", 5L,
                        LocalDate.of(2026, 9, 25),
                        "กินหมด", "กินหมด", 15,
                        "แปรงขน", "ร่าเริง", "ปกติ", null,
                        LocalDateTime.of(2026, 9, 25, 10, 0)
                );

        List<DailyCareReportResponse> reports = List.of(report);
        PageRequest firstPage = PageRequest.of(0, 5);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);
        when(reportService.getReportBookingsForOwner(7L, firstPage))
                .thenReturn(new PageImpl<>(
                        List.of(booking), firstPage, 1));
        when(reportService.getReportsForOwnerBookings(7L, List.of(3L)))
                .thenReturn(reports);

        mockMvc.perform(get("/reports")
                .principal(() -> "owner@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports"))
                .andExpect(model().attribute("reports", reports))
                .andExpect(model().attribute("page", 0))
                .andExpect(model().attribute("totalPages", 1));

        verify(reportService)
                .getReportBookingsForOwner(7L, firstPage);
        verify(reportService)
                .getReportsForOwnerBookings(7L, List.of(3L));
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

    @Test
    void showPetReportsShouldShowSelectedReport() throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(true);

        DailyCareReportResponse latest = new DailyCareReportResponse(
                11L, 2L, 3L, 4L, "Mochi", 5L,
                LocalDate.of(2026, 10, 3),
                "กินหมด", null, 10,
                null, null, null, null,
                LocalDateTime.of(2026, 10, 3, 10, 0));

        DailyCareReportResponse selected = new DailyCareReportResponse(
                10L, 2L, 3L, 4L, "Mochi", 5L,
                LocalDate.of(2026, 10, 2),
                "กินหมด", "กินหมด", 15,
                null, null, null, null,
                LocalDateTime.of(2026, 10, 2, 10, 0));

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);
        when(reportService.getReportsByBookingPet(2L, 7L))
                .thenReturn(List.of(latest, selected));

        mockMvc.perform(get("/reports/pets/2")
                .principal(() -> "owner@example.com")
                .param("reportId", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("report-detail"))
                .andExpect(model().attribute("selectedReport", selected))
                .andExpect(model().attribute("reportCount", 2))
                .andExpect(model().attribute(
                        "reportDates", List.of(latest, selected)));

        verify(reportService).getReportsByBookingPet(2L, 7L);
    }

    @Test
    void showPetReportsShouldAllowSelectingBeyondTenReports()
            throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(true);

        List<DailyCareReportResponse> reports =
                IntStream.range(0, 11)
                        .mapToObj(i -> new DailyCareReportResponse(
                                100L - i, 2L, 3L, 4L, "Mochi", 5L,
                                LocalDate.of(2026, 10, 15).minusDays(i),
                                null, null, null,
                                null, null, null, null,
                                LocalDateTime.of(2026, 10, 15, 10, 0)
                                        .minusDays(i)
                        ))
                        .toList();

        DailyCareReportResponse selected = reports.get(10);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);
        when(reportService.getReportsByBookingPet(2L, 7L))
                .thenReturn(reports);

        mockMvc.perform(get("/reports/pets/2")
                .principal(() -> "owner@example.com")
                .param("reportId", String.valueOf(selected.id())))
                .andExpect(status().isOk())
                .andExpect(view().name("report-detail"))
                .andExpect(model().attribute("selectedReport", selected))
                .andExpect(model().attribute("reportDates", reports));
    }

    @Test
    void showPetReportsShouldHideAnotherOwnersReports() throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(true);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);
        when(reportService.getReportsByBookingPet(99L, 7L))
                .thenThrow(new IllegalArgumentException(
                        "You cannot view this care report"));

        mockMvc.perform(get("/reports/pets/99")
                .principal(() -> "owner@example.com"))
                .andExpect(status().isNotFound());
    }
    @Test
    void showReportsShouldFilterBookingsByReportDate() throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(true);

        LocalDate reportDate = LocalDate.of(2026, 10, 3);
        PageRequest firstPage = PageRequest.of(0, 5);

        ReportBookingSummary booking = new ReportBookingSummary(
                3L,
                LocalDate.of(2026, 10, 2),
                LocalDate.of(2026, 10, 5)
        );

        DailyCareReportResponse report = new DailyCareReportResponse(
                11L, 2L, 3L, 4L, "Mochi", 5L,
                reportDate,
                null, null, null,
                null, null, null, null,
                LocalDateTime.of(2026, 10, 3, 10, 0)
        );

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);
        when(reportService.getReportBookingsForOwnerOnDate(
                7L, reportDate, firstPage))
                .thenReturn(new PageImpl<>(
                        List.of(booking), firstPage, 1));
        when(reportService.getReportsForOwnerBookings(
                7L, List.of(3L)))
                .thenReturn(List.of(report));

        mockMvc.perform(get("/reports")
                .principal(() -> "owner@example.com")
                .param("reportDate", "2026-10-03"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports"))
                .andExpect(model().attribute("reportDate", reportDate))
                .andExpect(model().attribute("totalBookings", 1L))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute(
                        "reports", List.of(report)));

        verify(reportService).getReportBookingsForOwnerOnDate(
                7L, reportDate, firstPage);
    }
}