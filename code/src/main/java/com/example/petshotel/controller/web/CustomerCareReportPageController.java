package com.example.petshotel.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller 
public class CustomerCareReportPageController {
    @GetMapping("/reports")
    public String showReports(){
        return "reports";
    }
}
