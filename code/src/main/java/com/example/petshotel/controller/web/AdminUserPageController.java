package com.example.petshotel.controller.web;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.petshotel.domain.enums.UserRole;
import com.example.petshotel.dto.request.UpdateUserRequest;
import com.example.petshotel.exception.DuplicateResourceException;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.UserService;

import jakarta.validation.Valid;

@Controller
public class AdminUserPageController {

    private final UserService userService;

    public AdminUserPageController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/admin/users")
    public String showUsers(@RequestParam(required = false) String keyword,
                            @RequestParam(defaultValue = "id") String sort,
                            Model model) {
        model.addAttribute("users", userService.searchUsers(keyword, sort));
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", sort);
        return "admin/users";
    }

    @GetMapping("/admin/users/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model,
                            RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("user", userService.getUserById(id));
            model.addAttribute("roles", UserRole.values());
            return "admin/user-edit";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/admin/users/{id}")
    public String updateUser(@PathVariable Long id,
                            @Valid @ModelAttribute UpdateUserRequest request,
                            BindingResult bindingResult,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/users/" + id + "/edit";
        }
        try {
            userService.updateUser(id, request, principal.getName());
            redirectAttributes.addFlashAttribute("message", "แก้ไขข้อมูลสมาชิกสำเร็จ");
            return "redirect:/admin/users";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        } catch (DuplicateResourceException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users/" + id + "/edit";
        }
    }
}