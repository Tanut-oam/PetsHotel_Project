package com.example.petshotel.service;

import com.example.petshotel.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse confirmPayment(Long bookingId);
}
