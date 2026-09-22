package com.example.petshotel.controller.api;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petshotel.dto.request.CreatePetRequest;
import com.example.petshotel.dto.request.UpdatePetRequest;
import com.example.petshotel.dto.response.PetResponse;
import com.example.petshotel.service.PetService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;


@RestController 
@RequestMapping("/api/owners/{ownerId}/pets")
public class PetRestController {
    private  final PetService petService;

    public PetRestController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping 
    public  ResponseEntity<PetResponse> createPet(@PathVariable Long ownerId,@Valid  @RequestBody CreatePetRequest request){
        PetResponse response = petService.createPet(ownerId, request);
        return  ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping 
    public List<PetResponse> getPetsByOwner(@PathVariable Long ownerId){
        return petService.getPetsByOwner(ownerId);
    }

    @GetMapping("/{petId}")
    public PetResponse getPetById(@PathVariable Long ownerId,@PathVariable Long petId){
        return  petService.getPetById(petId, ownerId);
    }

    @PutMapping("/{petId}")
    public  PetResponse updatePet(@PathVariable Long ownerId,@PathVariable Long petId,@Valid @RequestBody UpdatePetRequest request){
        return petService.updatePet(petId, ownerId, request);
    }

    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deactivatePet(@PathVariable Long ownerId,@PathVariable Long petId){
        petService.deactivatePet(petId, ownerId);;
        return ResponseEntity.noContent().build();
    }
    
}
