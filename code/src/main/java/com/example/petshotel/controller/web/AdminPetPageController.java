package com.example.petshotel.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.petshotel.service.PetService;

@Controller 
public class AdminPetPageController {
    
    private  final PetService petService;
    
    public AdminPetPageController(PetService petService) {
        this.petService = petService;
    }


    @GetMapping("/admin/pets")
    public String showPets(Model model){
        model.addAttribute("pets",petService.getActivePetsForAdmin());
        return "admin/pets";
    }
}
