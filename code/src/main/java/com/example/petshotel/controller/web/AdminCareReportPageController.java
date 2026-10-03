package com.example.petshotel.controller.web;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.request.CreateDailyCareReportRequest;
import com.example.petshotel.dto.request.UpdateDailyCareReportRequest;
import com.example.petshotel.dto.response.DailyCareReportResponse;
import com.example.petshotel.dto.response.ReportableBookingPetResponse;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.DailyCareReportService;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;

@Controller
public class AdminCareReportPageController {

    private final DailyCareReportService reportService;
    private final CurrentUserService currentUserService;

    public AdminCareReportPageController(
            DailyCareReportService reportService,
            CurrentUserService currentUserService) {
        this.reportService = reportService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/admin/reports")
    public String showReports(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "reportDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate reportDate,
            Principal principal,
            Model model
    ) {
        User user = requireActiveUser(principal);

        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        List<DailyCareReportResponse> reports =
                reportService.getAllReportsForStaffAndAdmin(user.getId());

        Map<Long, List<DailyCareReportResponse>> reportsByBookingPet =
                reports.stream().collect(
                        Collectors.groupingBy(
                                DailyCareReportResponse::bookingPetId
                        )
                );

        List<ReportableBookingPetResponse> bookingPetOptions =
                reportService.getReportableBookingPetsForStaffAndAdmin(
                        user.getId()
                );

        Map<Long, List<ReportableBookingPetResponse>> allBookings =
                bookingPetOptions.stream().collect(
                        Collectors.groupingBy(
                                ReportableBookingPetResponse::bookingId,
                                LinkedHashMap::new,
                                Collectors.toList()
                        )
                );

        String searchText = query == null ? "" : query.trim();
        String normalizedQuery = searchText.toLowerCase(Locale.ROOT);

        List<Map.Entry<Long, List<ReportableBookingPetResponse>>> matches =
                allBookings.entrySet().stream()
                        .filter(entry -> {
                            List<ReportableBookingPetResponse> pets =
                                    entry.getValue();
                            ReportableBookingPetResponse first = pets.get(0);

                            String petNames = pets.stream()
                                    .map(ReportableBookingPetResponse::petName)
                                    .collect(Collectors.joining(" "));

                            String searchable = (
                                    "การจอง #" + entry.getKey()
                                            + " " + first.ownerName()
                                            + " " + first.ownerEmail()
                                            + " " + petNames
                            ).toLowerCase(Locale.ROOT);

                            boolean matchesText =
                                    normalizedQuery.isEmpty()
                                            || searchable.contains(
                                                    normalizedQuery
                                            );

                            boolean matchesDate = reportDate == null
                                    || pets.stream().anyMatch(pet ->
                                            reportsByBookingPet.getOrDefault(
                                                    pet.bookingPetId(),
                                                    List.of()
                                            ).stream().anyMatch(report ->
                                                    reportDate.equals(
                                                            report.reportDate()
                                                    )
                                            )
                                    );

                            return matchesText && matchesDate;
                        })
                        .toList();

        int totalPages = (matches.size() + 4) / 5;

        if (page > 0 && page >= totalPages) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Map<Long, List<ReportableBookingPetResponse>> pageBookings =
                new LinkedHashMap<>();

        matches.stream()
                .skip((long) page * 5)
                .limit(5)
                .forEach(entry -> pageBookings.put(
                        entry.getKey(),
                        entry.getValue()
                ));

        Map<Long, Long> reportCountsByBooking = new LinkedHashMap<>();

        pageBookings.forEach((bookingId, pets) -> {
            long count = pets.stream()
                    .mapToLong(pet ->
                            reportsByBookingPet.getOrDefault(
                                    pet.bookingPetId(),
                                    List.of()
                            ).size()
                    )
                    .sum();

            reportCountsByBooking.put(bookingId, count);
        });

        model.addAttribute("reports", reports);
        model.addAttribute("reportsByBookingPet", reportsByBookingPet);
        model.addAttribute("bookingPetOptions", bookingPetOptions);
        model.addAttribute("reportableBookings", pageBookings);
        model.addAttribute("reportCountsByBooking", reportCountsByBooking);
        model.addAttribute("resultCount", matches.size());
        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("query", searchText);
        model.addAttribute("reportDate", reportDate);
        model.addAttribute(
                "hasFilters",
                !searchText.isEmpty() || reportDate != null
        );

        return "admin/reports";
    }

    @GetMapping("/admin/reports/pets/{bookingPetId}")
    public String showPetReports(
            @PathVariable Long bookingPetId,
            @RequestParam(name = "reportId", required = false) Long reportId,
            Principal principal,
            Model model) {
        User user = requireActiveUser(principal);

        ReportableBookingPetResponse pet = reportService
                .getReportableBookingPetsForStaffAndAdmin(user.getId())
                .stream()
                .filter(option -> option.bookingPetId().equals(bookingPetId))
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<DailyCareReportResponse> reports =
                reportService.getReportsByBookingPet(
                        bookingPetId, user.getId());

        Long openReportId = (Long) model.asMap().get("openReportId");
        Long selectedId =
                reportId != null ? reportId : openReportId;

        DailyCareReportResponse selectedReport = null;
        if (selectedId != null) {
            selectedReport = reports.stream()
                    .filter(report -> selectedId.equals(report.id()))
                    .findFirst()
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.NOT_FOUND));
        } else if (!reports.isEmpty()) {
            selectedReport = reports.get(0);
        }

        model.addAttribute("pet", pet);
        model.addAttribute("reports", reports);
        model.addAttribute("selectedReport", selectedReport);

        return "admin/report-detail";
    }

    @PostMapping("/admin/reports")
    public String createReport(
            Principal principal,
            @RequestParam(name = "bookingPetId", required = false) Long bookingPetId,
            @Valid @ModelAttribute CreateDailyCareReportRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        User user = requireActiveUser(principal);
        String target = petDetailUrl(bookingPetId);

        if (bookingPetId == null || bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "error", "กรุณาตรวจสอบสัตว์ในการจองและข้อมูลรายงาน");
            redirectAttributes.addFlashAttribute("openWrite", true);
            return "redirect:" + target;
        }

        try {
            reportService.createReport(bookingPetId, user.getId(), request);
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute(
                    "error", "ไม่พบสัตว์ในการจองที่เลือก");
            redirectAttributes.addFlashAttribute("openWrite", true);
            return "redirect:" + target;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "ไม่สามารถบันทึกรายงานได้ กรุณาตรวจสอบวันที่ การจอง และรายงานที่มีอยู่แล้ว");
            redirectAttributes.addFlashAttribute("openWrite", true);
            return "redirect:" + target;
        }

        redirectAttributes.addFlashAttribute(
                "message", "บันทึกรายงานการดูแลสำเร็จ");
        return "redirect:" + target;
    }

    @PostMapping("/admin/reports/{reportId}/edit")
    public String updateReport(
            @PathVariable Long reportId,
            Principal principal,
            @Valid @ModelAttribute UpdateDailyCareReportRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        User user = requireActiveUser(principal);
        String target = petDetailUrl(request.bookingPetId());

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "error", "กรุณาตรวจสอบข้อมูลรายงาน");
            redirectAttributes.addFlashAttribute("openReportId", reportId);
            return "redirect:" + target;
        }

        try {
            reportService.updateReport(reportId, user.getId(), request);
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute(
                    "error", "ไม่พบรายงานที่ต้องการแก้ไข");
            redirectAttributes.addFlashAttribute("openReportId", reportId);
            return "redirect:" + target;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute(
                    "error", "แก้ไขรายงานไม่สำเร็จ กรุณาตรวจสอบข้อมูล");
            redirectAttributes.addFlashAttribute("openReportId", reportId);
            return "redirect:" + target;
        }

        redirectAttributes.addFlashAttribute(
                "message", "แก้ไขรายงานสำเร็จ");
        return "redirect:" + target + "?reportId=" + reportId;
    }

    private String petDetailUrl(Long bookingPetId) {
        return bookingPetId == null
                ? "/admin/reports"
                : "/admin/reports/pets/" + bookingPetId;
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