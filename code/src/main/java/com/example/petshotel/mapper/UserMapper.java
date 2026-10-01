package com.example.petshotel.mapper;

import org.springframework.stereotype.Component;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.response.UserResponse;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getRole(),
            user.getActive(),
            user.getCreatedAt()
        );
    }
}