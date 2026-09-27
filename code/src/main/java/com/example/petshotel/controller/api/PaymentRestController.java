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

@RestController
@RequestMapping ("/api/bookings")
public class PaymentRestController {
    private final PaymentService paymentService;

    public PaymentRestController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{id}/payment/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable Long id,HttpServletRequest request) {

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
