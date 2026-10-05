package com.example.petshotel.controller.web;

import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.response.DailyCareReportResponse;
import com.example.petshotel.dto.response.ReportBookingSummary;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.DailyCareReportService;
import java.util.Objects;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.access.AccessDeniedException;

@Controller
@RequestMapping("/reports")
public class CustomerCareReportPageController {
    private final DailyCareReportService reportService;
    private final CurrentUserService currentUserService;

    public CustomerCareReportPageController(
            DailyCareReportService reportService,
            CurrentUserService currentUserService) {
        this.reportService = reportService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String showReports(
            @RequestParam(name = "page", defaultValue = "0") int page,@RequestParam(name = "reportDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate reportDate,
            Principal principal,
            Model model
    ) {
        User owner = requireActiveUser(principal);

        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        PageRequest pageRequest = PageRequest.of(page, 5);

        Page<ReportBookingSummary> bookingPage = reportDate == null
                ? reportService.getReportBookingsForOwner(
                        owner.getId(), pageRequest
                )
                : reportService.getReportBookingsForOwnerOnDate(
                        owner.getId(), reportDate, pageRequest
                );

        List<Long> bookingIds = bookingPage.getContent().stream()
                .map(booking -> Objects.requireNonNull(booking).id())
                .toList();

        List<DailyCareReportResponse> reports =
                reportService.getReportsForOwnerBookings(
                        owner.getId(), bookingIds
                );

        Map<Long, Map<Long, List<DailyCareReportResponse>>> bookingGroups =
                new LinkedHashMap<>();
        Map<Long, Long> reportCountsByBooking = new LinkedHashMap<>();
        Map<Long, ReportBookingSummary> bookingsById = new LinkedHashMap<>();

        for (ReportBookingSummary booking : bookingPage.getContent()) {
            bookingGroups.put(booking.id(), new LinkedHashMap<>());
            reportCountsByBooking.put(booking.id(), 0L);
            bookingsById.put(booking.id(), booking);
        }

        for (DailyCareReportResponse report : reports) {
            Map<Long, List<DailyCareReportResponse>> pets =
                    bookingGroups.get(report.bookingId());

            if (pets != null) {
                pets.computeIfAbsent(
                        report.bookingPetId(),
                        ignored -> new ArrayList<>()
                ).add(report);

                reportCountsByBooking.merge(
                        report.bookingId(),
                        1L,
                        (currentCount, increment) -> Long.sum(
                                Objects.requireNonNull(currentCount),
                                Objects.requireNonNull(increment)
                        )
                );
            }
        }

        model.addAttribute("reports", reports);
        model.addAttribute("bookingGroups", bookingGroups);
        model.addAttribute("reportCountsByBooking", reportCountsByBooking);
        model.addAttribute("bookingsById", bookingsById);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", bookingPage.getTotalPages());
        model.addAttribute("reportDate", reportDate);
        model.addAttribute("totalBookings", bookingPage.getTotalElements());
        return "reports";
    }

    @GetMapping("/pets/{bookingPetId}")
    public String showPetReports(
            @PathVariable Long bookingPetId,
            @RequestParam(name = "reportId", required = false) Long reportId,
            Principal principal,
            Model model
    ) {
        User owner = requireActiveUser(principal);

        List<DailyCareReportResponse> reports;
        try {
            reports = reportService.getReportsByBookingPet(
                    bookingPetId, owner.getId()
            );
        } catch (ResourceNotFoundException | AccessDeniedException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (reports.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        int selectedIndex = 0;

        if (reportId != null) {
            selectedIndex = -1;

            for (int i = 0; i < reports.size(); i++) {
                if (reports.get(i).id().equals(reportId)) {
                    selectedIndex = i;
                    break;
                }
            }

            if (selectedIndex == -1) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        }

        DailyCareReportResponse selectedReport = reports.get(selectedIndex);

        model.addAttribute("petName", reports.get(0).petName());
        model.addAttribute("bookingId", reports.get(0).bookingId());
        model.addAttribute("bookingPetId", bookingPetId);
        model.addAttribute("reportCount", reports.size());
        model.addAttribute("reportDates", reports);
        model.addAttribute("selectedReport", selectedReport);

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