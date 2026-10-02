package com.example.petshotel.config;

import java.io.IOException;
import java.net.URI;
import java.util.List;
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

    // path ที่เบราว์เซอร์ขอเองเบื้องหลัง ไม่ใช่หน้าที่ผู้ใช้ตั้งใจเปิด ห้ามพากลับไป
    private static final List<String> IGNORED_PATHS = List.of("/error", "/.well-known/", "/favicon");

    private final RequestCache requestCache = new HttpSessionRequestCache();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        requestCache.removeRequest(request, response);

        // ถ้าผู้ใช้ถูกเด้งมาล็อกอินจากหน้าที่ต้องล็อกอิน (เช่น กดจองห้อง) ให้กลับไปหน้านั้นพร้อมพารามิเตอร์เดิม
        if (savedRequest != null && isUserPage(savedRequest, request)) {
            response.sendRedirect(savedRequest.getRedirectUrl());
            return;
        }
        response.sendRedirect(request.getContextPath() + targetUrl(authentication));
    }

    private boolean isUserPage(SavedRequest savedRequest, HttpServletRequest request) {
        String path = URI.create(savedRequest.getRedirectUrl()).getPath();
        String pathInApp = path.substring(request.getContextPath().length());
        return IGNORED_PATHS.stream().noneMatch(pathInApp::startsWith);
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