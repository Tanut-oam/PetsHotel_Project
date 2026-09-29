package com.example.petshotel.service;

import com.example.petshotel.dto.request.RegisterRequest;

public interface AuthService {
    void register(RegisterRequest request);
}