package com.example.petshotel.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.petshotel.service.DashboardService;

@Controller 
@RequestMapping("/admin")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService){
        this.dashboardService = dashboardService;
    }

    @GetMapping 
    public String dashboard(Model model){
        model.addAttribute("dashboard", dashboardService.getDashboard());
        return "admin/index";
    }
}
