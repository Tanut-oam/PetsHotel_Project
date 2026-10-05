package com.example.petshotel.service.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.UserRole;
import com.example.petshotel.dto.request.UpdateUserRequest;
import com.example.petshotel.dto.response.UserResponse;
import com.example.petshotel.exception.DuplicateResourceException;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.mapper.UserMapper;
import com.example.petshotel.repository.UserRepository;
import com.example.petshotel.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String keyword, String sort) {
        Sort sortBy = toSort(sort);

        List<User> users = (keyword == null || keyword.isBlank())
                ? userRepository.findAll(sortBy)
                : userRepository.search(keyword.trim(), sortBy);

        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request, String currentAdminEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        String email = request.email().trim().toLowerCase();

        
        if (user.getEmail().equals(currentAdminEmail)
                && (request.role() != UserRole.ADMIN
                    || !request.active()
                    || !user.getEmail().equals(email))) {
            throw new IllegalStateException("You cannot change your role, suspend your account, or change your email address.");
        }

        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("อีเมลนี้ถูกใช้แล้ว: " + email);
        }

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPhoneNumber(request.phoneNumber().trim());
        user.setRole(request.role());
        user.setActive(request.active());

        return userMapper.toResponse(userRepository.save(user));
    }

    
    private Sort toSort(String sort) {
        if (sort == null) {
            return Sort.by("id");
        }
        return switch (sort) {
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "name"   -> Sort.by("firstName", "lastName");
            case "email"  -> Sort.by("email");
            case "role"   -> Sort.by("role", "firstName");
            default       -> Sort.by("id");
        };
    }
}