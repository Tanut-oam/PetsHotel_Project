package com.example.petshotel.controller.web;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.dto.response.DashboardResponse;
import com.example.petshotel.dto.response.RecentBookingResponse;
import com.example.petshotel.service.DashboardService;

public class DashboardControllerTest {

    private DashboardService dashboardService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        dashboardService = mock(DashboardService.class);

        DashboardController controller =
                new DashboardController(dashboardService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void showsDashboardWithDataFromService() throws Exception {
        int currentYear = LocalDate.now().getYear();

        RecentBookingResponse recentBooking = new RecentBookingResponse(
                25L,
                "Nadia Test",
                List.of("Mochi", "Lily"),
                "A101",
                BookingStatus.CHECKED_IN);

        DashboardResponse dashboard = new DashboardResponse(
                new BigDecimal("1500.00"),
                1L,
                2L,
                2L,
                List.of(recentBooking),
                currentYear,
                List.of(),
                new BigDecimal("1500.00"));

        when(dashboardService.getDashboard(currentYear))
                .thenReturn(dashboard);

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"))
                .andExpect(model().attribute("dashboard", dashboard));

        verify(dashboardService).getDashboard(currentYear);
    }

    @Test
    void showsDashboardWhenThereIsNoData() throws Exception {
        int currentYear = LocalDate.now().getYear();

        DashboardResponse dashboard = new DashboardResponse(
                BigDecimal.ZERO,
                0L,
                0L,
                0L,
                List.of(),
                currentYear,
                List.of(),
                BigDecimal.ZERO);

        when(dashboardService.getDashboard(currentYear))
                .thenReturn(dashboard);

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"))
                .andExpect(model().attribute("dashboard", dashboard));

        verify(dashboardService).getDashboard(currentYear);
    }

    @Test
    void showsDashboardForSelectedYear() throws Exception {
        DashboardResponse dashboard = new DashboardResponse(
                BigDecimal.ZERO,
                0L,
                0L,
                0L,
                List.of(),
                2025,
                List.of(),
                BigDecimal.ZERO);

        when(dashboardService.getDashboard(2025))
                .thenReturn(dashboard);

        mockMvc.perform(get("/admin").param("year", "2025"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"))
                .andExpect(model().attribute("dashboard", dashboard));

        verify(dashboardService).getDashboard(2025);
    }

    @Test
    void rejectsYearOutsideAllowedRange() throws Exception {
        mockMvc.perform(get("/admin").param("year", "0"))
                .andExpect(status().isBadRequest());
    }
}