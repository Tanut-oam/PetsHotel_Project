package com.example.petshotel.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import com.example.petshotel.domain.entity.Pet;
import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.PetType;
import com.example.petshotel.dto.request.CreatePetRequest;
import com.example.petshotel.dto.request.UpdatePetRequest;
import com.example.petshotel.dto.response.PetResponse;
import com.example.petshotel.mapper.PetMapper;
import com.example.petshotel.repository.PetRepository;
import com.example.petshotel.repository.UserRepository;
import com.example.petshotel.service.impl.PetServiceImpl;

class PetServiceTest {

    private PetRepository petRepository;
    private UserRepository userRepository;
    private PetServiceImpl petService;

    @BeforeEach
    void setUp() {
        petRepository = mock(PetRepository.class);
        userRepository = mock(UserRepository.class);

        petService = new PetServiceImpl(
            petRepository,
            userRepository,
            new PetMapper()
        );
    }

    @Test
    void createPetShouldSavePetWithCorrectOwner() {
        User owner = new User();
        owner.setId(1L);

        CreatePetRequest request = new CreatePetRequest(
            "โมจิ",
            PetType.DOG,
            3,
            8.5,
            "ชิบะ",
            "MALE",
            "ไม่มีโรคประจำตัว",
            "อาหารเช้าและเย็น",
            "กลัวเสียงดัง"
        );

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(owner));

        when(petRepository.save(any(Pet.class)))
            .thenAnswer(invocation -> {
                Pet pet = invocation.getArgument(0);
                pet.setId(10L);
                return pet;
            });

        PetResponse response = petService.createPet(1L, request);

        ArgumentCaptor<Pet> captor =
            ArgumentCaptor.forClass(Pet.class);

        verify(petRepository).save(captor.capture());

        Pet savedPet = captor.getValue();

        assertSame(owner, savedPet.getOwner());
        assertEquals("โมจิ", savedPet.getName());
        assertEquals(PetType.DOG, savedPet.getType());

        assertEquals(Long.valueOf(10L), response.id());
        assertEquals(Long.valueOf(1L), response.ownerId());
        assertEquals("โมจิ", response.name());
        assertEquals(request.breed(), response.breed());
        assertEquals(request.age(), response.age());
        assertEquals(request.weight(), response.weight());
        assertEquals(request.gender(), response.gender());
        assertEquals(request.medicalNote(), response.medicalNote());
        assertEquals(
            request.feedingInstruction(),
            response.feedingInstruction()
        );
        assertEquals(request.specialNote(), response.specialNote());
    }

    @Test
    void createPetShouldNotSaveWhenOwnerDoesNotExist() {
        CreatePetRequest request = new CreatePetRequest(
            "โมจิ",
            PetType.DOG,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        when(userRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(
            IllegalArgumentException.class,
            () -> petService.createPet(99L, request)
        );

        verify(petRepository, never()).save(any(Pet.class));
    }
    @Test
    void getPetByIdShouldReturnPetForItsOwner() {
    User owner = new User();
    owner.setId(1L);

    Pet pet = new Pet();
    pet.setId(10L);
    pet.setName("โมจิ");
    pet.setType(PetType.DOG);
    pet.setOwner(owner);

    when(petRepository.findById(10L))
        .thenReturn(Optional.of(pet));

    PetResponse response = petService.getPetById(10L, 1L);

    assertEquals(Long.valueOf(10L), response.id());
    assertEquals("โมจิ", response.name());
    assertEquals(Long.valueOf(1L), response.ownerId());

    verify(petRepository, never()).save(any(Pet.class));
    }
    @Test
    void getPetByIdShouldRejectDifferentOwner() {
    User owner = new User();
    owner.setId(1L);

    Pet pet = new Pet();
    pet.setId(10L);
    pet.setOwner(owner);

    when(petRepository.findById(10L))
        .thenReturn(Optional.of(pet));

    assertThrows(
        IllegalArgumentException.class,
        () -> petService.getPetById(10L, 2L)
    );

    verify(petRepository, never()).save(any(Pet.class));
    }
    @Test
    void getPetByIdShouldFailWhenPetDoesNotExist() {
    when(petRepository.findById(99L))
        .thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> petService.getPetById(99L, 1L)
    );

    verify(petRepository, never()).save(any(Pet.class));
    }
    @Test
    void getPetsByOwnerShouldReturnOwnersPets() {
    User owner = new User();
    owner.setId(1L);

    Pet firstPet = new Pet();
    firstPet.setId(10L);
    firstPet.setName("โมจิ");
    firstPet.setOwner(owner);

    Pet secondPet = new Pet();
    secondPet.setId(11L);
    secondPet.setName("โกโก้");
    secondPet.setOwner(owner);

    when(userRepository.existsById(1L)).thenReturn(true);

    when(petRepository.findByOwner_Id(1L))
        .thenReturn(List.of(firstPet, secondPet));

    List<PetResponse> responses = petService.getPetsByOwner(1L);

    assertEquals(2, responses.size());
    assertEquals("โมจิ", responses.get(0).name());
    assertEquals("โกโก้", responses.get(1).name());
    assertEquals(Long.valueOf(1L), responses.get(0).ownerId());
    assertEquals(Long.valueOf(1L), responses.get(1).ownerId());

    verify(petRepository).findByOwner_Id(1L);
    }
    @Test
    void getPetsByOwnerShouldReturnEmptyListWhenOwnerHasNoPets() {
    when(userRepository.existsById(1L)).thenReturn(true);

    when(petRepository.findByOwner_Id(1L))
        .thenReturn(List.of());

    List<PetResponse> responses = petService.getPetsByOwner(1L);

    assertTrue(responses.isEmpty());
    }
    @Test
    void getPetsByOwnerShouldFailWhenOwnerDoesNotExist() {
    when(userRepository.existsById(99L)).thenReturn(false);

    assertThrows(
        IllegalArgumentException.class,
        () -> petService.getPetsByOwner(99L)
    );

    verify(petRepository, never()).findByOwner_Id(any());
    }
    @Test
    void updatePetShouldUpdatePetForItsOwner() {
    User owner = new User();
    owner.setId(1L);

    Pet pet = new Pet();
    pet.setId(10L);
    pet.setName("ชื่อเดิม");
    pet.setType(PetType.DOG);
    pet.setOwner(owner);

    UpdatePetRequest request = new UpdatePetRequest(
        "โมจิ",
        PetType.DOG,
        "ชิบะ",
        4,
        9.5,
        "MALE",
        "แพ้ไก่",
        "อาหารเช้าและเย็น",
        "กลัวเสียงดัง"
    );

    when(petRepository.findById(10L))
        .thenReturn(Optional.of(pet));

    when(petRepository.save(any(Pet.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    PetResponse response =
        petService.updatePet(10L, 1L, request);

    ArgumentCaptor<Pet> captor =
        ArgumentCaptor.forClass(Pet.class);

    verify(petRepository).save(captor.capture());

    Pet savedPet = captor.getValue();

    assertEquals(Long.valueOf(10L), savedPet.getId());
    assertSame(owner, savedPet.getOwner());
    assertEquals("โมจิ", savedPet.getName());
    assertEquals("ชิบะ", savedPet.getBreed());
    assertEquals(Integer.valueOf(4), savedPet.getAge());
    assertEquals(Double.valueOf(9.5), savedPet.getWeight());
    assertEquals("แพ้ไก่", savedPet.getMedicalNote());

    assertEquals(Long.valueOf(10L), response.id());
    assertEquals(Long.valueOf(1L), response.ownerId());
    assertEquals("โมจิ", response.name());
    }
    @Test
    void updatePetShouldRejectDifferentOwner() {
    User owner = new User();
    owner.setId(1L);

    Pet pet = new Pet();
    pet.setId(10L);
    pet.setOwner(owner);

    UpdatePetRequest request = new UpdatePetRequest(
        "โมจิ",
        PetType.DOG,
        null,
        4,
        9.5,
        null,
        null,
        null,
        null
    );

    when(petRepository.findById(10L))
        .thenReturn(Optional.of(pet));

    assertThrows(
        IllegalArgumentException.class,
        () -> petService.updatePet(10L, 2L, request)
    );

    verify(petRepository, never()).save(any(Pet.class));
    }
    @Test
    void updatePetShouldFailWhenPetDoesNotExist() {
    UpdatePetRequest request = new UpdatePetRequest(
        "โมจิ",
        PetType.DOG,
        null,
        4,
        9.5,
        null,
        null,
        null,
        null
    );

    when(petRepository.findById(99L))
        .thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class,
        () -> petService.updatePet(99L, 1L, request)
    );

    verify(petRepository, never()).save(any(Pet.class));
    }
}