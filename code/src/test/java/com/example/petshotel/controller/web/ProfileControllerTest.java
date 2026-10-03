package com.example.petshotel.controller.web;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.CurrentUserService;

class ProfileControllerTest {

    private static final String EMAIL = "owner@example.com";

    private CurrentUserService currentUserService;
    private MockMvc mockMvc;

        @BeforeEach
    void setUp() {
        currentUserService = mock(CurrentUserService.class);

        // ใส่ prefix/suffix ให้ชื่อ view "profile" ไม่ชนกับ URL /profile (standalone ไม่มี Thymeleaf)
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/templates/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProfileController(currentUserService))
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    void profileShouldShowLoggedInUser() throws Exception {
        User user = mock(User.class);
        when(currentUserService.getByEmail(EMAIL)).thenReturn(user);

        mockMvc.perform(get("/profile").principal(() -> EMAIL))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attribute("user", user));

        verify(currentUserService).getByEmail(EMAIL);
    }

    @Test
    void profileShouldFailWhenLoggedInUserNoLongerExists() {
        when(currentUserService.getByEmail(EMAIL))
                .thenThrow(new ResourceNotFoundException("User", EMAIL));

        Exception exception = assertThrows(Exception.class,
                () -> mockMvc.perform(get("/profile").principal(() -> EMAIL)));

        Throwable cause = exception instanceof ResourceNotFoundException ? exception : exception.getCause();
        assertInstanceOf(ResourceNotFoundException.class, cause);
    }
}