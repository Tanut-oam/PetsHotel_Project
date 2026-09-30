package com.example.petshotel.controller.web;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.repository.UserRepository;
import com.example.petshotel.service.PetService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.petshotel.dto.request.CreatePetRequest;
import org.springframework.web.bind.annotation.PathVariable;
import com.example.petshotel.dto.request.UpdatePetRequest;
import com.example.petshotel.exception.ResourceNotFoundException;
import jakarta.validation.Valid;

@Controller
public class PetPageController {

    private final PetService petService;
    private final UserRepository userRepository;

    public PetPageController(PetService petService, UserRepository userRepository) {
        this.petService = petService;
        this.userRepository = userRepository;
    }

    @GetMapping("/pets")
    public String showPets(Principal principal, Model model) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User owner = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!Boolean.TRUE.equals(owner.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        model.addAttribute("pets", petService.getPetsByOwner(owner.getId()));
        return "pets";
    }

    @PostMapping("/pets")
    public  String createPet(Principal principal,@Valid  @ModelAttribute CreatePetRequest request,
            BindingResult bindingResult,RedirectAttributes redirectAttributes){

                if(principal == null){
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                }

                User owner = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
                if (!Boolean.TRUE.equals(owner.getActive())) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                 }

                if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute(
                        "error", "กรุณาตรวจสอบข้อมูลสัตว์เลี้ยง");
                return "redirect:/pets";
                }

                petService.createPet(owner.getId(), request);
                redirectAttributes.addFlashAttribute(
                        "message", "เพิ่มสัตว์เลี้ยงสำเร็จ");
                return "redirect:/pets";
    }

    @PostMapping("/pets/{petId}/edit")
    public String updatePet(
            @PathVariable Long petId,
            Principal principal,
            @Valid @ModelAttribute UpdatePetRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User owner = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!Boolean.TRUE.equals(owner.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "editError", "กรุณาตรวจสอบข้อมูลสัตว์เลี้ยง");
            redirectAttributes.addFlashAttribute("editPetId", petId);
            return "redirect:/pets";
        }

        try {
            petService.updatePet(petId, owner.getId(), request);
        } catch (ResourceNotFoundException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Pet not found", exception);
        }

        redirectAttributes.addFlashAttribute(
                "editMessage", "แก้ไขสัตว์เลี้ยงสำเร็จ");
        return "redirect:/pets";
    }
}