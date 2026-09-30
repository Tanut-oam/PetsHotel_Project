package com.example.petshotel.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;

class RoleBasedLoginSuccessHandlerTest {

    private final RoleBasedLoginSuccessHandler handler = new RoleBasedLoginSuccessHandler();

    private String redirectFor(String role) throws Exception {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "u@example.com", null, AuthorityUtils.createAuthorityList(role));
        MockHttpServletResponse response = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(new MockHttpServletRequest(), response, auth);
        return response.getRedirectedUrl();
    }

    @Test
    void adminShouldGoToAdminDashboard() throws Exception {
        assertEquals("/admin", redirectFor("ROLE_ADMIN"));
    }

    @Test
    void staffShouldGoToBookingManagement() throws Exception {
        assertEquals("/admin/bookings", redirectFor("ROLE_STAFF"));
    }

    @Test
    void customerShouldGoToHome() throws Exception {
        assertEquals("/", redirectFor("ROLE_CUSTOMER"));
    }
}