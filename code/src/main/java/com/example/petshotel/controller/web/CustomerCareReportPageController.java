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
import com.example.petshotel.dto.response.BookingResponse;
import com.example.petshotel.service.BookingService;

import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import com.example.petshotel.dto.response.DailyCareReportResponse;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller 
public class CustomerCareReportPageController {
    private final DailyCareReportService reportService;
    private final CurrentUserService currentUserService;
    private final BookingService bookingService;

    public CustomerCareReportPageController(DailyCareReportService reportService,
            CurrentUserService currentUserService,BookingService bookingService) {
        this.reportService = reportService;
        this.currentUserService = currentUserService;
        this.bookingService = bookingService;
    }

    @GetMapping("/reports")
    public String showReports(Principal principal, Model model) {
        User owner = requireActiveUser(principal);

        var reports = reportService.getReportsForOwner(owner.getId());
        model.addAttribute("reports", reports);
        model.addAttribute("bookingGroups",
        reports.stream().collect(Collectors.groupingBy(
                DailyCareReportResponse::bookingId,
                LinkedHashMap::new,
                Collectors.groupingBy(
                        DailyCareReportResponse::bookingPetId,
                        LinkedHashMap::new,
                        Collectors.toList()
                )
        )));
        model.addAttribute("reportCountsByBooking",
        reports.stream().collect(Collectors.groupingBy(
                DailyCareReportResponse::bookingId,
                Collectors.counting()
        )));
        
        model.addAttribute("bookingsById",
        bookingService.getBookingsByUserId(owner.getId()).stream()
                .collect(Collectors.toMap(
                        BookingResponse::getId,
                        booking -> booking
                )));

        return "reports";
    }

    @GetMapping("/reports/pets/{bookingPetId}")
    public String showPetReports(
            @PathVariable Long bookingPetId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "reportId", required = false) Long reportId,
            Principal principal,
            Model model) {

        User owner = requireActiveUser(principal);

        List<DailyCareReportResponse> reports;
        try {
            reports = reportService.getReportsByBookingPet(bookingPetId, owner.getId());
        } catch (ResourceNotFoundException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (reports.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        int pageSize = 10;
        int totalPages = (reports.size() + pageSize - 1) / pageSize;

        if (page < 0 || page >= totalPages) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        int from = page * pageSize;
        List<DailyCareReportResponse> pageReports =
                reports.subList(from, Math.min(from + pageSize, reports.size()));

        DailyCareReportResponse selectedReport = reportId == null
                ? pageReports.get(0)
                : pageReports.stream()
                        .filter(report -> report.id().equals(reportId))
                        .findFirst()
                        .orElseThrow(() ->
                                new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("petName", reports.get(0).petName());
        model.addAttribute("bookingId", reports.get(0).bookingId());
        model.addAttribute("bookingPetId", bookingPetId);
        model.addAttribute("reportCount", reports.size());
        model.addAttribute("pageReports", pageReports);
        model.addAttribute("selectedReport", selectedReport);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);

        return "report-detail";
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
