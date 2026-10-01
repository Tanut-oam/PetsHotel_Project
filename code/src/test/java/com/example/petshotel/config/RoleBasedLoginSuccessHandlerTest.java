package com.example.petshotel.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
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
    
    @Test
    void shouldReturnToSavedRequestAfterLogin() throws Exception {
        MockHttpServletRequest original = new MockHttpServletRequest("GET", "/bookings/new");
        original.setQueryString("roomId=1&checkIn=2026-10-10&checkOut=2026-10-12");
        new HttpSessionRequestCache().saveRequest(original, new MockHttpServletResponse());

        MockHttpServletRequest loginRequest = new MockHttpServletRequest("POST", "/login");
        loginRequest.setSession(original.getSession());
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "u@example.com", null, AuthorityUtils.createAuthorityList("ROLE_CUSTOMER"));

        handler.onAuthenticationSuccess(loginRequest, response, auth);

        assertTrue(response.getRedirectedUrl()
                .contains("/bookings/new?roomId=1&checkIn=2026-10-10&checkOut=2026-10-12"));
    }
}