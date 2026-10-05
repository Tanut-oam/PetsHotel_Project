package com.example.petshotel.controller.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.PetType;
import com.example.petshotel.dto.request.CreatePetRequest;
import com.example.petshotel.dto.request.UpdatePetRequest;
import com.example.petshotel.dto.response.PetResponse;
import com.example.petshotel.exception.GlobalExceptionHandler;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.PetService;

class PetRestControllerTest {

    private PetService petService;
    private CurrentUserService currentUserService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        petService = mock(PetService.class);
        currentUserService = mock(CurrentUserService.class);

        User owner = new User();
        owner.setId(1L);
        owner.setEmail("owner@example.com");
        owner.setActive(true);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);

        PetRestController controller =
                new PetRestController(petService, currentUserService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createPetShouldReturnCreatedPet() throws Exception {
        when(petService.createPet(
                eq(1L),
                any(CreatePetRequest.class)
        )).thenReturn(createPetResponse());

        mockMvc.perform(post("/api/owners/1/pets")
                .principal(() -> "owner@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Mochi",
                      "type": "DOG",
                      "age": 3,
                      "weight": 8.5,
                      "breed": "Shiba",
                      "gender": "MALE",
                      "medicalNote": "None",
                      "feedingInstruction": "Morning and evening",
                      "specialNote": "Afraid of loud noises"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Mochi"))
                .andExpect(jsonPath("$.ownerId").value(1))
                .andExpect(jsonPath("$.active").value(true));

        verify(petService).createPet(
                eq(1L),
                any(CreatePetRequest.class)
        );
    }

    @Test
    void createPetShouldRejectInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/owners/1/pets")
                .principal(() -> "owner@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "",
                      "type": "DOG",
                      "age": 3,
                      "weight": 8.5
                    }
                    """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(petService);
    }

    @Test
    void getPetsByOwnerShouldReturnPetList() throws Exception {
        when(petService.getPetsByOwner(1L))
                .thenReturn(List.of(createPetResponse()));

        mockMvc.perform(get("/api/owners/1/pets")
                .principal(() -> "owner@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Mochi"));

        verify(petService).getPetsByOwner(1L);
    }

    @Test
    void getPetByIdShouldPassPetIdBeforeOwnerId() throws Exception {
        when(petService.getPetById(10L, 1L))
                .thenReturn(createPetResponse());

        mockMvc.perform(get("/api/owners/1/pets/10")
                .principal(() -> "owner@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.ownerId").value(1));

        verify(petService).getPetById(10L, 1L);
    }

    @Test
    void updatePetShouldPassPetIdBeforeOwnerId() throws Exception {
        when(petService.updatePet(
                eq(10L),
                eq(1L),
                any(UpdatePetRequest.class)
        )).thenReturn(createPetResponse());

        mockMvc.perform(put("/api/owners/1/pets/10")
                .principal(() -> "owner@example.com")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Mochi",
                      "type": "DOG",
                      "breed": "Shiba",
                      "age": 3,
                      "weight": 8.5,
                      "gender": "MALE",
                      "medicalNote": "None",
                      "feedingInstruction": "Morning and evening",
                      "specialNote": "Afraid of loud noises"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Mochi"));

        verify(petService).updatePet(
                eq(10L),
                eq(1L),
                any(UpdatePetRequest.class)
        );
    }

    @Test
    void deactivatePetShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/owners/1/pets/10")
                .principal(() -> "owner@example.com"))
                .andExpect(status().isNoContent());

        verify(petService).deactivatePet(10L, 1L);
    }

    @Test
    void getPetsByOwnerShouldRejectDifferentOwner() throws Exception {
        mockMvc.perform(get("/api/owners/2/pets")
                .principal(() -> "owner@example.com"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(petService);
    }

    @Test
    void getPetsByOwnerShouldRejectMissingPrincipal() throws Exception {
        mockMvc.perform(get("/api/owners/1/pets"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(currentUserService, petService);
    }

    @Test
    void getPetsByOwnerShouldRejectUnknownUser() throws Exception {
        when(currentUserService.getByEmail("owner@example.com"))
                .thenThrow(new ResourceNotFoundException(
                        "User", "owner@example.com"));

        mockMvc.perform(get("/api/owners/1/pets")
                .principal(() -> "owner@example.com"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(petService);
    }

    @Test
    void getPetsByOwnerShouldRejectInactiveUser() throws Exception {
        User inactiveOwner = new User();
        inactiveOwner.setId(1L);
        inactiveOwner.setActive(false);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(inactiveOwner);

        mockMvc.perform(get("/api/owners/1/pets")
                .principal(() -> "owner@example.com"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(petService);
    }

    private PetResponse createPetResponse() {
        return new PetResponse(
                10L,
                "Mochi",
                PetType.DOG,
                "Shiba",
                3,
                8.5,
                "MALE",
                "None",
                "Morning and evening",
                "Afraid of loud noises",
                1L,
                "Karn Jaidee",
                true,
                null
        );
    }
}