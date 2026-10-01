package com.example.petshotel.config;

import java.io.IOException;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RoleBasedLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // ถ้าผู้ใช้ถูกเด้งมาล็อกอินจากหน้าที่ต้องล็อกอิน (เช่น กดจองห้อง) ให้กลับไปหน้านั้นพร้อมพารามิเตอร์เดิม
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            requestCache.removeRequest(request, response);
            response.sendRedirect(savedRequest.getRedirectUrl());
            return;
        }
        response.sendRedirect(request.getContextPath() + targetUrl(authentication));
    }

    String targetUrl(Authentication authentication) {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if (roles.contains("ROLE_ADMIN")) {
            return "/admin";
        }
        if (roles.contains("ROLE_STAFF")) {
            return "/admin/bookings";
        }
        return "/";
    }
}