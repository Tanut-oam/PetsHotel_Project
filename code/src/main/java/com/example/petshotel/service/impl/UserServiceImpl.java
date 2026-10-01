package com.example.petshotel.service.impl;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.dto.response.UserResponse;
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
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll(Sort.by("id")).stream()
                .map(userMapper::toResponse)
                .toList();
    }
}