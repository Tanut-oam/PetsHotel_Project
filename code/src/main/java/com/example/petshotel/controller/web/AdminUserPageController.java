package com.example.petshotel.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.petshotel.service.UserService;

@Controller
public class AdminUserPageController {

    private final UserService userService;

    public AdminUserPageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/users")
    public String showUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/users";
    }
}