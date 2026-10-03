package com.example.petshotel.controller.api;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.request.CreateDailyCareReportRequest;
import com.example.petshotel.dto.request.UpdateDailyCareReportRequest;
import com.example.petshotel.dto.response.DailyCareReportResponse;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.DailyCareReportService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Care Reports", description = "บันทึกและดูรายงานการดูแลสัตว์เลี้ยงรายวัน")
@RestController 
@RequestMapping("/api/care-reports")
public class DailyCareReportRestController {
    private  final DailyCareReportService reportService;
    private final CurrentUserService currentUserService;

    public DailyCareReportRestController(DailyCareReportService reportService,CurrentUserService currentUserService) {
        this.reportService = reportService;
        this.currentUserService = currentUserService;
    }

    @Operation(summary = "เขียนรายงานการดูแล",
        description = "เฉพาะ STAFF / ADMIN · เขียนรายงานให้สัตว์เลี้ยงในการจอง "
                + "· การจองต้องอยู่สถานะ CHECKED_IN หรือ CHECKED_OUT "
                + "· วันที่รายงานต้องอยู่ในช่วงเข้าพัก "
                + "· สัตว์เลี้ยงหนึ่งตัวมีรายงานได้ไม่เกินหนึ่งฉบับต่อวัน")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "สร้างรายงานสำเร็จ"),
        @ApiResponse(responseCode = "400", description = "ข้อมูลหรือวันที่รายงานไม่ถูกต้อง"),
        @ApiResponse(responseCode = "401", description = "ยังไม่ได้เข้าสู่ระบบ"),
        @ApiResponse(responseCode = "403", description = "ไม่ใช่ STAFF / ADMIN หรือ CSRF token ไม่ถูกต้อง"),
        @ApiResponse(responseCode = "404", description = "ไม่พบสัตว์เลี้ยงในการจอง"),
        @ApiResponse(responseCode = "409", description = "สถานะการจองไม่รองรับ หรือมีรายงานของสัตว์เลี้ยงในวันนี้แล้ว")
    })
    @PostMapping("/booking-pets/{bookingPetId}")
    public ResponseEntity<DailyCareReportResponse> createReport(@PathVariable  Long bookingPetId,Principal principal,
            @Valid @RequestBody  CreateDailyCareReportRequest request){

                DailyCareReportResponse response = reportService.createReport(bookingPetId, currentUserId(principal), request);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }


    @Operation(summary = "ดูรายงานการดูแลหนึ่งฉบับ",
        description = "STAFF / ADMIN ดูรายงานได้ "
                + "· ลูกค้าดูได้เฉพาะรายงานของการจองที่ตนเป็นเจ้าของ")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "ดึงรายงานสำเร็จ"),
        @ApiResponse(responseCode = "400", description = "รหัสไม่ถูกต้องหรือไม่มีสิทธิ์อ่านรายงานตามกฎปัจจุบัน"),
        @ApiResponse(responseCode = "401", description = "ยังไม่ได้เข้าสู่ระบบ"),
        @ApiResponse(responseCode = "403", description = "บัญชีถูกปิดใช้งาน"),
        @ApiResponse(responseCode = "404", description = "ไม่พบรายงาน")
    })
    @GetMapping("/{reportId}")
    public DailyCareReportResponse getReportById(@PathVariable Long reportId,Principal principal){
        return reportService.getReportById(reportId, currentUserId(principal));
    }


    @Operation(summary = "ดูรายงานทั้งหมดของสัตว์เลี้ยงในการจอง",
        description = "แสดงรายงานเรียงจากวันที่ใหม่ไปเก่า "
                + "· STAFF / ADMIN ดูได้ "
                + "· ลูกค้าดูได้เฉพาะรายงานของการจองที่ตนเป็นเจ้าของ")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "ดึงรายการรายงานสำเร็จ"),
        @ApiResponse(responseCode = "400", description = "รหัสไม่ถูกต้องหรือไม่มีสิทธิ์อ่านรายงานตามกฎปัจจุบัน"),
        @ApiResponse(responseCode = "401", description = "ยังไม่ได้เข้าสู่ระบบ"),
        @ApiResponse(responseCode = "403", description = "บัญชีถูกปิดใช้งาน"),
        @ApiResponse(responseCode = "404", description = "ไม่พบสัตว์เลี้ยงในการจอง")
    })
    @GetMapping("/booking-pets/{bookingPetId}")
    public List<DailyCareReportResponse> getReportsByBookingPet(@PathVariable  Long bookingPetId,Principal principal){
        return  reportService.getReportsByBookingPet(bookingPetId, currentUserId(principal));
    }


    @Operation(summary = "แก้ไขรายงานการดูแล",
        description = "เฉพาะ STAFF / ADMIN · แก้ไขรายงานที่มีอยู่ "
                + "· สัตว์เลี้ยงที่เลือกต้องอยู่ในการจองเดิม "
                + "· วันที่รายงานต้องอยู่ในช่วงเข้าพัก "
                + "· ห้ามซ้ำกับรายงานของสัตว์เลี้ยงตัวเดียวกันในวันเดียวกัน")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "แก้ไขรายงานสำเร็จ"),
        @ApiResponse(responseCode = "400", description = "ข้อมูลไม่ถูกต้อง วันที่อยู่นอกช่วงเข้าพัก หรือไม่มีสิทธิ์แก้ไข"),
        @ApiResponse(responseCode = "401", description = "ยังไม่ได้เข้าสู่ระบบ"),
        @ApiResponse(responseCode = "403", description = "บัญชีถูกปิดใช้งานหรือ CSRF token ไม่ถูกต้อง"),
        @ApiResponse(responseCode = "404", description = "ไม่พบรายงานหรือสัตว์เลี้ยงในการจอง"),
        @ApiResponse(responseCode = "409", description = "มีรายงานของสัตว์เลี้ยงในวันที่เลือกแล้ว")
    })
    @PutMapping("/{reportId}")
    public  DailyCareReportResponse updateReport(@PathVariable  Long reportId,Principal principal,
            @Valid  @RequestBody UpdateDailyCareReportRequest request){
                return reportService.updateReport(reportId, currentUserId(principal), request);
            }

     private Long currentUserId(Principal principal) {
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

        return user.getId();
    }
}

