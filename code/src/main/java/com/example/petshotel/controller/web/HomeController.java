 package com.example.petshotel.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.petshotel.service.RoomService;

@Controller
public class HomeController {

    private static final int FEATURED_ROOM_LIMIT = 3;

    private final RoomService roomService;

    public HomeController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("rooms", roomService.getActiveRooms().stream()
                .limit(FEATURED_ROOM_LIMIT)
                .toList());
        return "index";
    }
}