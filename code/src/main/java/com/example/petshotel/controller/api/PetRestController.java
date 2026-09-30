package com.example.petshotel.controller.api;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.request.CreatePetRequest;
import com.example.petshotel.dto.request.UpdatePetRequest;
import com.example.petshotel.dto.response.PetResponse;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.PetService;
import com.example.petshotel.exception.ResourceNotFoundException;
import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/owners/{ownerId}/pets")
public class PetRestController {
    private  final PetService petService;
    private final CurrentUserService currentUserService;

    public PetRestController(PetService petService,CurrentUserService currentUserService) {
        this.petService = petService;
        this.currentUserService = currentUserService;
    }

    @PostMapping 
    public  ResponseEntity<PetResponse> createPet(@PathVariable Long ownerId,Principal principal,@Valid  @RequestBody CreatePetRequest request){
        requireCurrentOwner(ownerId, principal);
        PetResponse response = petService.createPet(ownerId, request);
        return  ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping 
    public List<PetResponse> getPetsByOwner(@PathVariable Long ownerId,Principal principal){
        requireCurrentOwner(ownerId, principal);
        return petService.getPetsByOwner(ownerId);
    }

    @GetMapping("/{petId}")
    public PetResponse getPetById(@PathVariable Long ownerId,@PathVariable Long petId,Principal principal){
        requireCurrentOwner(ownerId, principal);
        return  petService.getPetById(petId, ownerId);
    }

    @PutMapping("/{petId}")
    public  PetResponse updatePet(@PathVariable Long ownerId,@PathVariable Long petId,Principal principal,@Valid @RequestBody UpdatePetRequest request){
        requireCurrentOwner(ownerId, principal);
        return petService.updatePet(petId, ownerId, request);
    }

    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deactivatePet(@PathVariable Long ownerId,@PathVariable Long petId,Principal principal){
        requireCurrentOwner(ownerId, principal);
        petService.deactivatePet(petId, ownerId);
        return ResponseEntity.noContent().build();
    }
    
    private  void requireCurrentOwner(Long ownerId, Principal principal){
        if(principal == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        User currentUser;
        try {
            currentUser = currentUserService.getByEmail(
                    principal.getName());
        } catch (ResourceNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        if (!Boolean.TRUE.equals(currentUser.getActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (!ownerId.equals(currentUser.getId())) {
            throw new AccessDeniedException(
                    "Cannot access another owner's pets");
        }
    }
}
