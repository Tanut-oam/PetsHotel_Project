package com.example.petshotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.UserRole;
import com.example.petshotel.dto.request.RegisterRequest;
import com.example.petshotel.exception.DuplicateResourceException;
import com.example.petshotel.repository.UserRepository;
import com.example.petshotel.service.impl.AuthServiceImpl;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthServiceImpl(userRepository, passwordEncoder);
    }

    private RegisterRequest request(String email) {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("ธเนศ");
        request.setLastName("ทดสอบ");
        request.setEmail(email);
        request.setPhoneNumber("0812345678");
        request.setPassword("password123");
        request.setConfirmPassword("password123");
        return request;
    }

    @Test
    void registerShouldSaveCustomerWithEncodedPassword() {
        when(userRepository.existsByEmail("tanut@example.com")).thenReturn(false);

        authService.register(request("  Tanut@Example.com "));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("tanut@example.com", saved.getEmail());
        assertEquals(UserRole.CUSTOMER, saved.getRole());
        assertTrue(saved.getActive());
        assertTrue(passwordEncoder.matches("password123", saved.getPassword()));
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail("tanut@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
            () -> authService.register(request("tanut@example.com")));
        verify(userRepository, never()).save(any());
    }
}