package com.example.petshotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import com.example.petshotel.dto.response.UserResponse;
import com.example.petshotel.mapper.UserMapper;
import com.example.petshotel.repository.UserRepository;
import com.example.petshotel.service.impl.UserServiceImpl;

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

        List<UserResponse> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("ตัวอย่าง ผู้ใช้", result.get(0).fullName());
        assertEquals(UserRole.CUSTOMER, result.get(0).role());
    }
}