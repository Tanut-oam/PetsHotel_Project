package com.example.petshotel.controller.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ExtendedModelMap;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.PetType;
import com.example.petshotel.dto.response.PetResponse;
import com.example.petshotel.repository.UserRepository;
import com.example.petshotel.service.PetService;

class PetPageControllerTest {

    private final PetService petService = mock(PetService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new PetPageController(petService, userRepository))
            .build();

    @Test
    void showPetsShouldLoadPetsOfLoggedInOwner() {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(true);

        PetResponse pet = new PetResponse(
                10L, "Mochi", PetType.DOG, "Shiba", 2, 12.5,
                "MALE", null, null, null, 7L, "Test Owner", true
        );
        List<PetResponse> pets = List.of(pet);

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));
        when(petService.getPetsByOwner(7L)).thenReturn(pets);

        ExtendedModelMap model = new ExtendedModelMap();
        PetPageController controller =
                new PetPageController(petService, userRepository);

        String template = controller.showPets(
                () -> "owner@example.com", model);

        assertEquals("pets", template);
        assertSame(pets, model.get("pets"));
        verify(petService).getPetsByOwner(7L);
    }

    @Test
    void showPetsShouldRejectMissingLogin() throws Exception {
        mockMvc.perform(get("/pets"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(userRepository, petService);
    }

    @Test
    void showPetsShouldRejectInactiveOwner() throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(false);

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(Optional.of(owner));

        mockMvc.perform(get("/pets")
                .principal(() -> "owner@example.com"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(petService);
    }
}