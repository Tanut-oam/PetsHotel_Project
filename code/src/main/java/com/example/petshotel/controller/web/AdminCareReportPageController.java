package com.example.petshotel.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller 
public class AdminCareReportPageController {
    
    @GetMapping("/admin/reports")
    public String showReports(){
        return "admin/reports";
    }
}
