package com.example.petshotel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.UserRole;
import com.example.petshotel.dto.request.UpdateUserRequest;
import com.example.petshotel.dto.response.UserResponse;
import com.example.petshotel.exception.DuplicateResourceException;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.mapper.UserMapper;
import com.example.petshotel.repository.UserRepository;
import com.example.petshotel.service.impl.UserServiceImpl;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;



@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Spy
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getAllUsers_returnsAllUsersWithoutPassword() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("ตัวอย่าง");
        user.setLastName("ผู้ใช้");
        user.setEmail("demo@petshotel.test");
        user.setPassword("hashed");
        user.setPhoneNumber("0812345678");
        user.setRole(UserRole.CUSTOMER);
        when(userRepository.findAll(any(Sort.class))).thenReturn(List.of(user));

        List<UserResponse> result = userService.searchUsers(null, "id");

        assertEquals(1, result.size());
        assertEquals("ตัวอย่าง ผู้ใช้", result.get(0).fullName());
        assertEquals(UserRole.CUSTOMER, result.get(0).role());
    }

        private User sampleUser(Long id, String email, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setFirstName("Kittirat");
        user.setLastName("Khamto");
        user.setEmail(email);
        user.setPassword("hashed");
        user.setPhoneNumber("0987257896");
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    @Test
    void searchUsers_withKeyword_usesSearchQuery() {
        User user = sampleUser(1L, "a@example.com", UserRole.CUSTOMER);
        when(userRepository.search(eq("kit"), any(Sort.class))).thenReturn(List.of(user));

        List<UserResponse> result = userService.searchUsers("  kit  ", "name");

        assertEquals(1, result.size());
        verify(userRepository).search(eq("kit"), any(Sort.class));
        verify(userRepository, never()).findAll(any(Sort.class));
    }

    @Test
    void updateUser_success() {
        User user = sampleUser(2L, "b@example.com", UserRole.CUSTOMER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UpdateUserRequest request = new UpdateUserRequest("New", "Name",
                "B@Example.com", "0811111111", UserRole.STAFF, true);

        userService.updateUser(2L, request, "admin@example.com");

        assertEquals("b@example.com", user.getEmail());
        assertEquals(UserRole.STAFF, user.getRole());
        assertEquals("New", user.getFirstName());
    }

    @Test
    void updateUser_duplicateEmail_throws() {
        User user = sampleUser(2L, "b@example.com", UserRole.CUSTOMER);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        UpdateUserRequest request = new UpdateUserRequest("New", "Name",
                "taken@example.com", "0811111111", UserRole.CUSTOMER, true);

        assertThrows(DuplicateResourceException.class,
                () -> userService.updateUser(2L, request, "admin@example.com"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_adminCannotDemoteSelf() {
        User admin = sampleUser(1L, "admin@example.com", UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        UpdateUserRequest request = new UpdateUserRequest("Kittirat", "Khamto",
                "admin@example.com", "0987257896", UserRole.CUSTOMER, true);

        assertThrows(IllegalStateException.class,
                () -> userService.updateUser(1L, request, "admin@example.com"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_notFound_throws() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UpdateUserRequest request = new UpdateUserRequest("A", "B",
                "c@example.com", "0811111111", UserRole.CUSTOMER, true);

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUser(99L, request, "admin@example.com"));
    }
}