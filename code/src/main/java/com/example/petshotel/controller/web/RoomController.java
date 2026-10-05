package com.example.petshotel.controller.web;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import com.example.petshotel.dto.request.CreateRoomRequest;
import com.example.petshotel.dto.request.UpdateRoomRequest;
import com.example.petshotel.dto.request.UpdateStatusRequest;
import com.example.petshotel.dto.response.RoomResponse;
import com.example.petshotel.exception.DuplicateResourceException;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.RoomService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;


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
    public String createRoom(@Valid @ModelAttribute CreateRoomRequest request,BindingResult bindingResult,@RequestParam(value = "image", required = false) MultipartFile image,RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", firstError(bindingResult));
            return "redirect:/admin/rooms/new";
        }
        RoomResponse created;
        try {
            created = roomService.createRoom(request);
        } catch (ResourceNotFoundException | DuplicateResourceException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/rooms";
        }
        if (image != null && !image.isEmpty()) {
            try {
                roomService.updateRoomImage(created.id(), image);
            } catch (IllegalArgumentException | IllegalStateException e) {
                redirectAttributes.addFlashAttribute("error",
                        "สร้างห้องแล้ว แต่อัปโหลดรูปไม่สำเร็จ: " + e.getMessage());
                return "redirect:/admin/rooms/" + created.id() + "/edit";
            }
        }
        redirectAttributes.addFlashAttribute("message", "สร้างห้องสำเร็จ");
        return "redirect:/admin/rooms";
    }

    
    @GetMapping("/admin/rooms/{id}/edit")
    public String editRoomForm(@PathVariable Long id,Model model) {
        model.addAttribute("room", roomService.getRoomById(id));
        return "rooms/edit";
    }
    
    @PostMapping("/admin/rooms/{id}")
    public String updateRoom(@PathVariable Long id, @Valid @ModelAttribute UpdateRoomRequest request,BindingResult bindingResult,@RequestParam(value = "image", required = false) MultipartFile image,RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", firstError(bindingResult));
            return "redirect:/admin/rooms/" + id + "/edit";
        }
        try {
            roomService.updateRoom(id, request);
            if (image != null && !image.isEmpty()) {
                roomService.updateRoomImage(id, image);
            }
            redirectAttributes.addFlashAttribute("message", "แก้ไขห้องสำเร็จ");
        } catch (ResourceNotFoundException | DuplicateResourceException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error",
                    "บันทึกข้อมูลห้องแล้ว แต่อัปโหลดรูปไม่สำเร็จ: " + e.getMessage());
            return "redirect:/admin/rooms/" + id + "/edit";
        }
        return "redirect:/admin/rooms";
    }
    
    @PostMapping("/admin/rooms/{id}/status")
    public String updateStatus(@PathVariable Long id,@Valid @ModelAttribute UpdateStatusRequest request,BindingResult bindingResult,RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", firstError(bindingResult));
            return "redirect:/admin/rooms";
        }
        try{
            roomService.setRoomStatus(id, request);
            redirectAttributes.addFlashAttribute("message", "เปลี่ยนสถานะห้องสำเร็จ");
        } catch(ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/rooms";
    }

    private String firstError(BindingResult bindingResult) {
        return bindingResult.getAllErrors().get(0).getDefaultMessage();
    }
    
    @PostMapping("/admin/rooms/{id}/deactivate")
    public String deactivateRoom(@PathVariable Long id,RedirectAttributes redirectAttributes) {
        try{
            roomService.deactivateRoom(id);
            redirectAttributes.addFlashAttribute("message", "ปิดใช้งานห้องสำเร็จ");
        } catch(ResourceNotFoundException e){
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/admin/rooms";
    }

    @PostMapping("/admin/rooms/{id}/image/delete")
    public String removeRoomImage(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.removeRoomImage(id);
            redirectAttributes.addFlashAttribute("message", "ลบรูปห้องแล้ว");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/rooms";
        }
        return "redirect:/admin/rooms/" + id + "/edit";
    }
}
