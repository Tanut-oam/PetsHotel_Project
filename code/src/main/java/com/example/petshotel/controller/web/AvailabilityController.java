package com.example.petshotel.controller.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.dto.request.AvailabilitySearchRequest;
import com.example.petshotel.mapper.AvailabilityMapper;
import com.example.petshotel.service.AvailabilityService;

@Controller
@RequestMapping("/rooms/available")
public class AvailabilityController {

    private final AvailabilityService availabilityService;
    private final AvailabilityMapper availabilityMapper;

    public AvailabilityController(AvailabilityService availabilityService, AvailabilityMapper availabilityMapper) {
        this.availabilityService = availabilityService;
        this.availabilityMapper = availabilityMapper;
    }

    @GetMapping
    public String search(@ModelAttribute("search") AvailabilitySearchRequest search, BindingResult bindingResult, Model model) {
        // เข้าหน้าครั้งแรก ยังไม่ได้กรอกฟอร์ม
        if (search.checkIn() == null && search.checkOut() == null && search.petCount() == null) {
            return "availability/search";
        }
        if (bindingResult.hasErrors() || search.checkIn() == null || search.checkOut() == null || search.petCount() == null) {
            model.addAttribute("error", "กรุณากรอกวันที่และจำนวนสัตว์เลี้ยงให้ครบถ้วน");
            return "availability/search";
        }
        try {
            List<Room> rooms = availabilityService.findAvailableRooms(search.checkIn(), search.checkOut(), search.petCount());
            model.addAttribute("result", availabilityMapper.toResponse(search, rooms));
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "availability/search";
    }
}