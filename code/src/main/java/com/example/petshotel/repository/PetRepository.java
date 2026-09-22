package com.example.petshotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.petshotel.domain.entity.Pet;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByOwner_Id(Long ownerId);
    List<Pet> findByOwner_IdAndActiveTrue(Long ownerId);
}
