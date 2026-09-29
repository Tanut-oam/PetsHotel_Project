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
}