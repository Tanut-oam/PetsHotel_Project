package com.example.petshotel.controller.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.web.WebAttributes;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.petshotel.dto.request.RegisterRequest;
import com.example.petshotel.exception.DuplicateResourceException;
import com.example.petshotel.service.AuthService;

class AuthControllerTest {

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();
    }

    @Test
    void loginPageShouldRender() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void registerPageShouldHaveEmptyForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void validRegistrationShouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/register")
                .param("firstName", "ธเนศ")
                .param("lastName", "ทดสอบ")
                .param("email", "tanut@example.com")
                .param("phoneNumber", "0812345678")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void mismatchedPasswordShouldShowError() throws Exception {
        mockMvc.perform(post("/register")
                .param("firstName", "ธเนศ")
                .param("lastName", "ทดสอบ")
                .param("email", "tanut@example.com")
                .param("phoneNumber", "0812345678")
                .param("password", "password123")
                .param("confirmPassword", "different99"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(model().attributeHasFieldErrors("registerRequest", "confirmPassword"));

        verifyNoInteractions(authService);
    }

    @Test
    void duplicateEmailShouldShowErrorOnEmailField() throws Exception {
        doThrow(new DuplicateResourceException("อีเมลนี้ถูกใช้สมัครแล้ว"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/register")
                .param("firstName", "ธเนศ")
                .param("lastName", "ทดสอบ")
                .param("email", "tanut@example.com")
                .param("phoneNumber", "0812345678")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("registerRequest", "email"));
    }
    
    @Test
    void loginErrorShouldShowSuspendedMessageWhenAccountDisabled() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, new DisabledException("disabled"));

        mockMvc.perform(get("/login").param("error", "").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("loginError", AuthController.ACCOUNT_DISABLED_MESSAGE));
    }

    @Test
    void loginErrorShouldShowBadCredentialsMessageForWrongPassword() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, new BadCredentialsException("bad"));

        mockMvc.perform(get("/login").param("error", "").session(session))
                .andExpect(status().isOk())
                .andExpect(model().attribute("loginError", AuthController.BAD_CREDENTIALS_MESSAGE));
    }

    @Test
    void loginPageWithoutErrorShouldNotShowMessage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("loginError"));
    }
}