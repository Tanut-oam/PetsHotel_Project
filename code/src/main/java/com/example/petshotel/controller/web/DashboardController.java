package com.example.petshotel.controller.web;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.example.petshotel.service.DashboardService;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String dashboard(
            @RequestParam(name = "year", required = false) Integer year,
            Model model) {

        int selectedYear = LocalDate.now().getYear();

        if (year != null) {
            selectedYear = year;
        }

        if (selectedYear < 1 || selectedYear > 9999) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Year must be between 1 and 9999");
        }

        model.addAttribute(
                "dashboard",
                dashboardService.getDashboard(selectedYear));

        return "admin/index";
    }
}