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
public class CustomerCareReportPageController {
    private final DailyCareReportService reportService;
    private final CurrentUserService currentUserService;

    public CustomerCareReportPageController(DailyCareReportService reportService,
            CurrentUserService currentUserService) {
        this.reportService = reportService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/reports")
    public String showReports(Principal principal, Model model) {
        User owner = requireActiveUser(principal);

        model.addAttribute("reports",reportService.getReportsForOwner(owner.getId()));

        return "reports";
    }

    private User requireActiveUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User user;
        try {
            user = currentUserService.getByEmail(principal.getName());
        } catch (ResourceNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return user;
    }

}
