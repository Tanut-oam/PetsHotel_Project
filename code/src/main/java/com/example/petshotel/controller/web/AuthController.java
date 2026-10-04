package com.example.petshotel.controller.web;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.petshotel.dto.request.RegisterRequest;
import com.example.petshotel.exception.DuplicateResourceException;
import com.example.petshotel.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    static final String ACCOUNT_DISABLED_MESSAGE = "บัญชีนี้ถูกระงับการใช้งาน กรุณาติดต่อผู้ดูแลระบบ";
    static final String BAD_CREDENTIALS_MESSAGE = "อีเมลหรือรหัสผ่านไม่ถูกต้อง";

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            HttpServletRequest request,
                            Model model) {
        if (error != null) {
            model.addAttribute("loginError", loginErrorMessage(request));
        }
        return "auth/login";
    }

    // Spring Security เก็บสาเหตุที่ล็อกอินไม่ผ่านไว้ใน session หลัง redirect มาที่ /login?error
    private String loginErrorMessage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return BAD_CREDENTIALS_MESSAGE;
        }
        Object exception = session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);

        return exception instanceof DisabledException ? ACCOUNT_DISABLED_MESSAGE : BAD_CREDENTIALS_MESSAGE;
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                           BindingResult bindingResult) {
        if (request.getPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "รหัสผ่านไม่ตรงกัน");
        }
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(request);
        } catch (DuplicateResourceException ex) {
            bindingResult.rejectValue("email", "duplicate", ex.getMessage());
            return "auth/register";
        }
        return "redirect:/login?registered";
    }
}