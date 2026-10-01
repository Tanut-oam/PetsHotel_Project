package com.example.petshotel.controller.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.petshotel.config.SecurityConfig;
import com.example.petshotel.domain.enums.UserRole;
import com.example.petshotel.dto.response.UserResponse;
import com.example.petshotel.service.UserService;

@WebMvcTest(AdminUserPageController.class)
@Import(SecurityConfig.class)
class AdminUserPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanSeeAllUsers() throws Exception {
        List<UserResponse> users = List.of(new UserResponse(1L, "ตัวอย่าง", "ผู้ใช้",
                "demo@petshotel.test", "0812345678", UserRole.CUSTOMER, true, null));
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attribute("users", users));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void customerCannotSeeUsers() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }
}