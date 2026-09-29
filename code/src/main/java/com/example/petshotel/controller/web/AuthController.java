package com.example.petshotel.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.petshotel.dto.request.RegisterRequest;
import com.example.petshotel.exception.DuplicateResourceException;
import com.example.petshotel.service.AuthService;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
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