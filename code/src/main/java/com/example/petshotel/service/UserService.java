package com.example.petshotel.service;

import java.util.List;

import com.example.petshotel.dto.response.UserResponse;

public interface UserService {
    List<UserResponse> getAllUsers();
}