package com.example.petshotel.service.impl;

import org.springframework.stereotype.Service;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.repository.UserRepository;
import com.example.petshotel.service.CurrentUserService;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }
}