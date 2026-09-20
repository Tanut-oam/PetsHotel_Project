package com.example.petshotel.mapper;
import org.springframework.stereotype.Component;
import com.example.petshotel.domain.entity.Pet;
import com.example.petshotel.dto.response.PetResponse;

@Component
public class PetMapper {
    public  PetResponse toResponse(Pet pet){
        return new PetResponse(
            pet.getId(),
            pet.getName(),
            pet.getType(),
            pet.getBreed(),
            pet.getAge(),
            pet.getWeight(),
            pet.getGender(),
            pet.getMedicalNote(),
            pet.getFeedingInstruction(),
            pet.getSpecialNote(),
            pet.getOwner().getId()
        );
    }
}
