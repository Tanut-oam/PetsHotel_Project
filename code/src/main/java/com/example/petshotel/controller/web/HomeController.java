package com.example.petshotel.controller.web;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.petshotel.mapper.ExtraServiceMapper;
import com.example.petshotel.service.ExtraServiceService;
import com.example.petshotel.service.RoomService;

@Controller
public class HomeController {

    private static final int FEATURED_ROOM_LIMIT = 3;
    private static final int FEATURED_SERVICE_LIMIT = 4;

    private final RoomService roomService;
    private final ExtraServiceService extraServiceService;
    private final ExtraServiceMapper extraServiceMapper;

    public HomeController(RoomService roomService,
                        ExtraServiceService extraServiceService,
                        ExtraServiceMapper extraServiceMapper) {
        this.roomService = roomService;
        this.extraServiceService = extraServiceService;
        this.extraServiceMapper = extraServiceMapper;
    }

    @GetMapping("/")
    public String home(Model model, Authentication authentication) {
        // ยังไม่ล็อกอิน → ไปหน้า login ก่อน (หลังล็อกอินจะไปหน้าแรกตาม role)
        if (authentication == null) {
            return "redirect:/login";
        }
        if (isAdmin(authentication)) {
            return "redirect:/admin";
        }

        model.addAttribute("rooms", roomService.getActiveRooms().stream()
                .limit(FEATURED_ROOM_LIMIT)
                .toList());
        model.addAttribute("services", extraServiceService.getActiveExtraServices().stream()
                .limit(FEATURED_SERVICE_LIMIT)
                .map(extraServiceMapper::toResponse)
                .toList());
        return "index";
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}