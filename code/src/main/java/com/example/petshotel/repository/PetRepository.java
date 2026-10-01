package com.example.petshotel.repository;

import java.util.List;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.petshotel.domain.entity.Pet;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByOwner_Id(Long ownerId);
    List<Pet> findByOwner_IdAndActiveTrue(Long ownerId);

    @EntityGraph(attributePaths = "owner")
    List<Pet> findByActiveTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p
            FROM Pet p
            WHERE p.id IN :petIds
            ORDER BY p.id
            """)
    List<Pet> findAllByIdForUpdate(
            @Param("petIds") Collection<Long> petIds
    );
}
