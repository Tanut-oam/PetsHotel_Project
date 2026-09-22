package com.example.petshotel.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.entity.Pet;
import com.example.petshotel.domain.entity.User;
import com.example.petshotel.dto.request.CreatePetRequest;
import com.example.petshotel.dto.request.UpdatePetRequest;
import com.example.petshotel.dto.response.PetResponse;
import com.example.petshotel.mapper.PetMapper;
import com.example.petshotel.repository.PetRepository;
import com.example.petshotel.repository.UserRepository;
import com.example.petshotel.service.PetService;

@Service 
public class PetServiceImpl implements PetService {
    private  final PetRepository petRepository;
    private  final UserRepository userRepository;
    private  final PetMapper petMapper;
    
    public PetServiceImpl(PetRepository petRepository, UserRepository userRepository, PetMapper petMapper) {
        this.petRepository = petRepository;
        this.userRepository = userRepository;
        this.petMapper = petMapper;
    }

    @Override 
    @Transactional 
    public PetResponse createPet(Long ownerId, CreatePetRequest request){
        User owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new IllegalArgumentException("Owner not found: " + ownerId));

        Pet pet = new  Pet();
        pet.setName(request.name());
        pet.setType(request.type());
        pet.setBreed(request.breed());
        pet.setAge(request.age());
        pet.setWeight(request.weight());
        pet.setGender(request.gender());
        pet.setMedicalNote(request.medicalNote());
        pet.setFeedingInstruction(request.feedingInstruction());
        pet.setSpecialNote(request.specialNote());
        pet.setOwner(owner);

        Pet savedPet = petRepository.save(pet);
        return petMapper.toResponse(savedPet);
    }

    @Override 
    @Transactional(readOnly = true)
    public List<PetResponse> getPetsByOwner(Long ownerId){
        if (!userRepository.existsById(ownerId)) {
            throw new IllegalArgumentException(
                "Owner not found: " + ownerId
            );
        }

        List<Pet> pets = petRepository.findByOwner_Id(ownerId);
        List<PetResponse> responses = new  ArrayList<>();

        for (Pet pet: pets){
            responses.add(petMapper.toResponse(pet));
        }
        return responses;
    }

    @Override 
    @Transactional(readOnly = true)
    public PetResponse getPetById(Long petId, Long ownerId){
        Pet pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found" + petId));

        if (!pet.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Pet not found: " + petId);
        }
        return petMapper.toResponse(pet);
    }
    
    @Override 
    @Transactional 
    public PetResponse updatePet(Long petId,Long ownerId,UpdatePetRequest request){
        Pet pet = petRepository.findById(petId)
            .orElseThrow(() -> new IllegalArgumentException("Pet not found: " + petId));
        
        if (!pet.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Pet not found: " +petId);
        }
        pet.setName(request.name());
        pet.setType(request.type());
        pet.setBreed(request.breed());
        pet.setAge(request.age());
        pet.setWeight(request.weight());
        pet.setGender(request.gender());
        pet.setMedicalNote(request.medicalNote());
        pet.setFeedingInstruction(request.feedingInstruction());
        pet.setSpecialNote(request.specialNote());
        Pet savedPet = petRepository.save(pet);
        return petMapper.toResponse(savedPet);
    }
}
