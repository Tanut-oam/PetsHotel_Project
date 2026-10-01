package com.example.petshotel.controller.web;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import com.example.petshotel.dto.request.UpdateUserRequest;

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
                when(userService.searchUsers(null, "id")).thenReturn(users);

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

        @Test
    @WithMockUser(roles = "ADMIN")
    void searchPassesKeywordAndSort() throws Exception {
        when(userService.searchUsers("kit", "name")).thenReturn(List.of());

        mockMvc.perform(get("/admin/users").param("keyword", "kit").param("sort", "name"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("keyword", "kit"))
                .andExpect(model().attribute("sort", "name"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void updateUser_success_redirectsToList() throws Exception {
        mockMvc.perform(post("/admin/users/2")
                        .with(csrf())
                        .param("firstName", "New")
                        .param("lastName", "Name")
                        .param("email", "b@example.com")
                        .param("phoneNumber", "0811111111")
                        .param("role", "STAFF")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"))
                .andExpect(flash().attribute("message", "แก้ไขข้อมูลสมาชิกสำเร็จ"));

        verify(userService).updateUser(eq(2L), any(UpdateUserRequest.class), eq("admin@example.com"));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void updateUser_invalidPhone_redirectsBackToEdit() throws Exception {
        mockMvc.perform(post("/admin/users/2")
                        .with(csrf())
                        .param("firstName", "New")
                        .param("lastName", "Name")
                        .param("email", "b@example.com")
                        .param("phoneNumber", "123")
                        .param("role", "STAFF")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users/2/edit"));

        verify(userService, never()).updateUser(any(), any(), any());
    }
}