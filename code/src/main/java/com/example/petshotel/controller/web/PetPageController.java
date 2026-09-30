package com.example.petshotel.controller.web;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.request.CreatePetRequest;
import com.example.petshotel.dto.request.UpdatePetRequest;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.PetService;

import jakarta.validation.Valid;

@Controller
public class PetPageController {

    private final PetService petService;
    private final CurrentUserService currentUserService;

    public PetPageController(PetService petService,CurrentUserService currentUserService) {
        this.petService = petService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/pets")
    public String showPets(Principal principal, Model model) {
        User owner = requireActiveOwner(principal);

        model.addAttribute("pets", petService.getPetsByOwner(owner.getId()));
        return "pets";
    }

    @PostMapping("/pets")
    public String createPet(Principal principal,@Valid @ModelAttribute CreatePetRequest request,
            BindingResult bindingResult,RedirectAttributes redirectAttributes) {

        User owner = requireActiveOwner(principal);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "กรุณาตรวจสอบข้อมูลสัตว์เลี้ยง");
            return "redirect:/pets";
        }

        petService.createPet(owner.getId(), request);
        redirectAttributes.addFlashAttribute("message", "เพิ่มสัตว์เลี้ยงสำเร็จ");
        return "redirect:/pets";
    }

    @PostMapping("/pets/{petId}/edit")
    public String updatePet(@PathVariable Long petId,Principal principal,@Valid @ModelAttribute UpdatePetRequest request,
            BindingResult bindingResult,RedirectAttributes redirectAttributes) {

        User owner = requireActiveOwner(principal);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("editError", "กรุณาตรวจสอบข้อมูลสัตว์เลี้ยง");
            redirectAttributes.addFlashAttribute("editPetId", petId);
            return "redirect:/pets";
        }

        try {
            petService.updatePet(petId, owner.getId(), request);
        } catch (ResourceNotFoundException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Pet not found", exception);
        }

        redirectAttributes.addFlashAttribute("editMessage", "แก้ไขสัตว์เลี้ยงสำเร็จ");
        return "redirect:/pets";
    }

    @PostMapping("/pets/{petId}/delete")
    public String deletePet(@PathVariable Long petId,Principal principal,
            RedirectAttributes redirectAttributes) {

        User owner = requireActiveOwner(principal);

        try {
            petService.deactivatePet(petId, owner.getId());
        } catch (ResourceNotFoundException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Pet not found", exception);
        }

        redirectAttributes.addFlashAttribute("deleteMessage", "นำสัตว์เลี้ยงออกจากรายการแล้ว");
        return "redirect:/pets";
    }

    private User requireActiveOwner(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User owner;
        try {
            owner = currentUserService.getByEmail(principal.getName());
        } catch (ResourceNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (!Boolean.TRUE.equals(owner.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return owner;
    }
}