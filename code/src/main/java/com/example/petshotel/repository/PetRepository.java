package com.example.petshotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.petshotel.domain.entity.Pet;
import org.springframework.data.jpa.repository.EntityGraph;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByOwner_Id(Long ownerId);
    List<Pet> findByOwner_IdAndActiveTrue(Long ownerId);

    @EntityGraph(attributePaths = "owner")
    List<Pet> findByActiveTrue();
}
