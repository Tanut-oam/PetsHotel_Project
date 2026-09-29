package com.example.petshotel.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.UserRole;
import com.example.petshotel.dto.request.RegisterRequest;
import com.example.petshotel.exception.DuplicateResourceException;
import com.example.petshotel.repository.UserRepository;
import com.example.petshotel.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        // เก็บอีเมลเป็นตัวพิมพ์เล็ก ให้ตรงกับ CustomUserDetailsService ที่ค้นหาแบบตัวพิมพ์เล็ก
        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("อีเมลนี้ถูกใช้สมัครแล้ว");
        }

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(email);
        user.setPhoneNumber(request.getPhoneNumber().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);

        userRepository.save(user);
    }
}