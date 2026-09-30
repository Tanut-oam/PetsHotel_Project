package com.example.petshotel.controller.web;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.example.petshotel.dto.request.CreateRoomRequest;
import com.example.petshotel.dto.request.UpdateRoomRequest;
import com.example.petshotel.dto.request.UpdateStatusRequest;
import com.example.petshotel.service.RoomService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;




@Controller
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    // ===== ลูกค้า =====
    @GetMapping("/rooms")
    public String publicList(Model model) {
        model.addAttribute("rooms",roomService.getActiveRooms());
        return "rooms";
    }

    @GetMapping("/rooms/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("room", roomService.getRoomById(id));
        return "room-detail";
    }

    // ===== Admin =====
    @GetMapping("/admin/rooms")
        public String adminList(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());
        return "admin/rooms";
    }


    @GetMapping("/admin/rooms/new")
    public String newRoomForm() {
        return "rooms/form";
    }

    @PostMapping("/admin/rooms")
    public String createRoom(@ModelAttribute CreateRoomRequest request,RedirectAttributes redirectAttributes) {
        try{
            roomService.createRoom(request);
            redirectAttributes.addFlashAttribute("message", "สร้างห้องสำเร็จ");
        } catch (IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/rooms";
    }
    
    @GetMapping("/admin/rooms/{id}/edit")
    public String editRoomForm(@PathVariable Long id,Model model) {
        model.addAttribute("room", roomService.getRoomById(id));
        return "rooms/edit";
    }
    
    @PostMapping("/admin/rooms/{id}")
    public String updateRoom(@PathVariable Long id,@ModelAttribute UpdateRoomRequest request,RedirectAttributes redirectAttributes) {
        try{
            roomService.updateRoom(id, request);
            redirectAttributes.addFlashAttribute("message", "แก้ไขห้องสำเร็จ");
        } catch(IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/rooms";
    }
    
    @PostMapping("/admin/rooms/{id}/status")
    public String updateStatus(@PathVariable Long id,@ModelAttribute UpdateStatusRequest request,RedirectAttributes redirectAttributes) {
        try{
            roomService.setRoomStatus(id, request);
            redirectAttributes.addFlashAttribute("message", "เปลี่ยนสถานะห้องสำเร็จ");
        } catch(IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/rooms";
    }
    
    @PostMapping("/admin/rooms/{id}/deactivate")
    public String deactivateRoom(@PathVariable Long id,RedirectAttributes redirectAttributes) {
        try{
            roomService.deactivateRoom(id);
            redirectAttributes.addFlashAttribute("message", "ปิดใช้งานห้องสำเร็จ");
        } catch(IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/rooms";
    }
}
