package com.example.petshotel.service;

import com.example.petshotel.dto.request.CreatePetRequest;
import com.example.petshotel.dto.request.UpdatePetRequest;
import com.example.petshotel.dto.response.PetResponse;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface PetService {
    PetResponse createPet(Long ownerId, CreatePetRequest request);
    List<PetResponse> getPetsByOwner(Long ownerId);
    List<PetResponse> getActivePetsForAdmin();
    PetResponse getPetById(Long petId, Long ownerId);
    PetResponse updatePet(Long petId,Long ownerId,UpdatePetRequest request);
    void deactivatePet(Long petId, Long ownerId);
    PetResponse updatePetImage(Long petId, Long ownerId, MultipartFile image);
} 
