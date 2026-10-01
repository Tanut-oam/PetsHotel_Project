package com.example.petshotel.controller.web;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.DailyCareReportService;


@Controller 
public class AdminCareReportPageController {
    
    private final DailyCareReportService reportService;
    private  final CurrentUserService currentUserService;
    
    public AdminCareReportPageController(DailyCareReportService reportService, CurrentUserService currentUserService) {
        this.reportService = reportService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/admin/reports")
    public String showReports(Principal principal,Model model){
        if(principal == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User user;
        try{
            user = currentUserService.getByEmail(principal.getName());
        }catch(ResourceNotFoundException exeption){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        model.addAttribute(
                "reports",
                reportService.getAllReportsForStaffAndAdmin(user.getId()));

        return "admin/reports";
    }
    
}
