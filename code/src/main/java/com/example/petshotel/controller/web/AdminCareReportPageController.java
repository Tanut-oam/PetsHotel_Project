package com.example.petshotel.controller.web;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.request.CreateDailyCareReportRequest;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.DailyCareReportService;
import com.example.petshotel.dto.response.DailyCareReportResponse;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import com.example.petshotel.dto.response.ReportableBookingPetResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.petshotel.dto.request.UpdateDailyCareReportRequest;

@Controller 
public class AdminCareReportPageController {
    
    private final DailyCareReportService reportService;
    private  final CurrentUserService currentUserService;
    
    public AdminCareReportPageController(DailyCareReportService reportService, CurrentUserService currentUserService) {
        this.reportService = reportService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/admin/reports")
    public String showReports(Principal principal, Model model) {
        User user = requireActiveUser(principal);

        var reports = reportService.getAllReportsForStaffAndAdmin(user.getId());
        model.addAttribute("reports", reports);
        model.addAttribute(
                "reportsByBookingPet",
                reports.stream().collect(
                        Collectors.groupingBy(
                                DailyCareReportResponse::bookingPetId)));

        var bookingPetOptions =
                reportService.getReportableBookingPetsForStaffAndAdmin(user.getId());
        model.addAttribute("bookingPetOptions", bookingPetOptions);
        model.addAttribute(
                "reportableBookings",
                bookingPetOptions.stream().collect(
                        Collectors.groupingBy(
                                ReportableBookingPetResponse::bookingId,
                                LinkedHashMap::new,
                                Collectors.toList())));

        return "admin/reports";
    }

    @PostMapping("/admin/reports")
    public String createReport(
            Principal principal,
            @RequestParam(name = "bookingPetId", required = false) Long bookingPetId,
            @Valid @ModelAttribute CreateDailyCareReportRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        User user = requireActiveUser(principal);
        if (bookingPetId != null) {
            redirectAttributes.addFlashAttribute(
                    "openBookingPetId", bookingPetId);
        }

        if (bookingPetId == null || bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "error", "กรุณาตรวจสอบสัตว์ในการจองและข้อมูลรายงาน");
            return "redirect:/admin/reports";
        }

        try {
            reportService.createReport(bookingPetId, user.getId(), request);
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute("error", "ไม่พบสัตว์ในการจองที่เลือก");
            return "redirect:/admin/reports";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute("error","ไม่สามารถบันทึกรายงานได้ กรุณาตรวจสอบวันที่ การจอง และรายงานที่มีอยู่แล้ว");
            return "redirect:/admin/reports";
        }
        redirectAttributes.addFlashAttribute("message", "บันทึกรายงานการดูแลสำเร็จ");
        return "redirect:/admin/reports";
    }

    @PostMapping("/admin/reports/{reportId}/edit")
    public String updateReport(
            @PathVariable Long reportId,
            Principal principal,
            @Valid @ModelAttribute UpdateDailyCareReportRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        User user = requireActiveUser(principal);

        if (request.bookingPetId() != null) {
            redirectAttributes.addFlashAttribute(
                    "openBookingPetId", request.bookingPetId());
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "error", "กรุณาตรวจสอบข้อมูลรายงาน");
            redirectAttributes.addFlashAttribute("openReportId", reportId);
            return "redirect:/admin/reports";
        }

        try {
            reportService.updateReport(reportId, user.getId(), request);
        } catch (ResourceNotFoundException exception) {
            redirectAttributes.addFlashAttribute(
                    "error", "ไม่พบรายงานที่ต้องการแก้ไข");
            redirectAttributes.addFlashAttribute("openReportId", reportId);
            return "redirect:/admin/reports";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            redirectAttributes.addFlashAttribute(
                    "error", "แก้ไขรายงานไม่สำเร็จ กรุณาตรวจสอบข้อมูล");
            redirectAttributes.addFlashAttribute("openReportId", reportId);
            return "redirect:/admin/reports";
        }

        redirectAttributes.addFlashAttribute(
                "message", "แก้ไขรายงานสำเร็จ");
        return "redirect:/admin/reports";
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
