package com.example.petshotel.controller.web;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;


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
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;


    @GetMapping
    public String listRooms(Model model) {
        model.addAttribute("rooms",roomService.getAllRooms());
        return "rooms/list";
    }

    //แสดงฟอมสร้าง rooms ใหม่เด้อ
    @GetMapping("/new")
    public String newRoomForm() {
        return "rooms/form";
    }

    //รับข้อมูลจากฟอมมาสร้างห้อง
    @PostMapping("")
    public String createRoom(@ModelAttribute CreateRoomRequest request,RedirectAttributes redirectAttributes) {
        try{
            roomService.createRoom(request);
            redirectAttributes.addFlashAttribute("message", "สร้างห้องสำเร็จ");
        } catch (IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/rooms";
    }
    
    @GetMapping("/{id}/edit")
    public String editRoomForm(@PathVariable Long id,Model model) {
        model.addAttribute("room", roomService.getRoomById(id));
        return "rooms/edit";
    }
    
    @PostMapping("/{id}")
    public String updateRoom(@PathVariable Long id,@ModelAttribute UpdateRoomRequest request,RedirectAttributes redirectAttributes) {
        try{
            roomService.updateRoom(id, request);
            redirectAttributes.addFlashAttribute("message", "แก้ไขห้องสำเร็จ");
        } catch(IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/rooms";
    }
    
    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,@ModelAttribute UpdateStatusRequest request,RedirectAttributes redirectAttributes) {
        try{
            roomService.setRoomStatus(id, request);
            redirectAttributes.addFlashAttribute("message", "เปลี่ยนสถานะห้องสำเร็จ");
        } catch(IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rooms";
    }
    
    @PostMapping("/{id}/deactivate")
    public String deactivateRoom(@PathVariable Long id,RedirectAttributes redirectAttributes) {
        try{
            roomService.deactivateRoom(id);
            redirectAttributes.addFlashAttribute("message", "ปิดใช้งานห้องสำเร็จ");
        } catch(IllegalArgumentException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/rooms";
    }
}
