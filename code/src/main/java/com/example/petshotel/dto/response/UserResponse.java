package com.example.petshotel.dto.response;

import java.time.LocalDateTime;

import com.example.petshotel.domain.enums.UserRole;

public record UserResponse(Long id, String firstName, String lastName, String email,
        String phoneNumber, UserRole role, Boolean active, LocalDateTime createdAt) {

    public String fullName() {
        return firstName + " " + lastName;
    }
}