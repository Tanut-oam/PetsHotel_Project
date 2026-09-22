package com.example.petshotel.controller.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.example.petshotel.domain.enums.PetType;
import com.example.petshotel.dto.request.CreatePetRequest;
import com.example.petshotel.dto.request.UpdatePetRequest;
import com.example.petshotel.dto.response.PetResponse;
import com.example.petshotel.service.PetService;

class PetRestControllerTest {

    private PetService petService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        petService = org.mockito.Mockito.mock(PetService.class);

        PetRestController controller =
                new PetRestController(petService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void createPetShouldReturnCreatedPet() throws Exception {
        PetResponse response = createPetResponse();

        when(petService.createPet(
                eq(1L),
                any(CreatePetRequest.class)
        )).thenReturn(response);

        mockMvc.perform(post("/api/owners/1/pets")
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

        mockMvc.perform(get("/api/owners/1/pets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].name").value("Mochi"));

        verify(petService).getPetsByOwner(1L);
    }

    @Test
    void getPetByIdShouldPassPetIdBeforeOwnerId() throws Exception {
        when(petService.getPetById(10L, 1L))
                .thenReturn(createPetResponse());

        mockMvc.perform(get("/api/owners/1/pets/10"))
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
        mockMvc.perform(delete("/api/owners/1/pets/10"))
                .andExpect(status().isNoContent());

        verify(petService).deactivatePet(10L, 1L);
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
                true
        );
    }
}