package com.example.petshotel.service;

import java.util.Optional;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.repository.UserRepository;
import com.example.petshotel.service.impl.CurrentUserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrentUserServiceTest {

    private UserRepository userRepository;
    private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        currentUserService = new CurrentUserServiceImpl(userRepository);
    }

    @Test
    void getByEmailShouldReturnUserWhenFound() {
        User user = mock(User.class);
        when(userRepository.findByEmail("a@example.com")).thenReturn(Optional.of(user));

        assertSame(user, currentUserService.getByEmail("a@example.com"));
    }

    @Test
    void getByEmailShouldThrowWhenNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> currentUserService.getByEmail("missing@example.com"));
        assertEquals("User not found: missing@example.com", ex.getMessage());
    }
}