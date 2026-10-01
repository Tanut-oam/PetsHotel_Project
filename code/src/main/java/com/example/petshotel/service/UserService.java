package com.example.petshotel.service;

import java.util.List;

import com.example.petshotel.dto.request.UpdateUserRequest;
import com.example.petshotel.dto.response.UserResponse;

public interface UserService {
    List<UserResponse> searchUsers(String keyword, String sort);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UpdateUserRequest request, String currentAdminEmail);
}