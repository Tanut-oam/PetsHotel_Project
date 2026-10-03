package com.example.petshotel.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petshotel.dto.response.PaymentResponse;
import com.example.petshotel.service.PaymentService;

import jakarta.servlet.http.HttpServletRequest;

import com.example.petshotel.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping ("/api/bookings")
@Tag(name = "Payments", description = "ยืนยันการรับชำระเงินและออกใบเสร็จ")
public class PaymentRestController {
    private final PaymentService paymentService;

    public PaymentRestController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{id}/payment/confirm")
    @Operation(summary = "ยืนยันรับชำระเงิน",
            description = "เฉพาะ STAFF / ADMIN · บันทึกว่าจ่ายเต็มจำนวนและออกใบเสร็จ "
                    + "· รับได้เมื่อการจองเป็น CONFIRMED, CHECKED_IN หรือ CHECKED_OUT "
                    + "· ถ้ายืนยันซ้ำจะได้ใบเสร็จเดิม")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ยืนยันสำเร็จ พร้อมข้อมูลใบเสร็จ"),
            @ApiResponse(responseCode = "401", description = "ยังไม่ล็อกอิน", content = @Content),
            @ApiResponse(responseCode = "403", description = "ไม่ใช่ STAFF หรือ ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "ไม่พบการจอง",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "สถานะการจองยังรับชำระไม่ได้ (เช่น PENDING หรือ CANCELLED)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PaymentResponse> confirmPayment(@Parameter(description = "รหัสการจอง", example = "1") @PathVariable Long id,HttpServletRequest request) {

        if (request.getUserPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!request.isUserInRole("STAFF")
                && !request.isUserInRole("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(paymentService.confirmPayment(id));
    }

    
}
