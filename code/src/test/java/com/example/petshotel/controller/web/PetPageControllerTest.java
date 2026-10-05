package com.example.petshotel.controller.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ExtendedModelMap;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.PetType;
import com.example.petshotel.dto.request.CreatePetRequest;
import com.example.petshotel.dto.request.UpdatePetRequest;
import com.example.petshotel.dto.response.PetResponse;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.service.PetService;

class PetPageControllerTest {

    private final PetService petService = mock(PetService.class);
    private final CurrentUserService currentUserService =
            mock(CurrentUserService.class);

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(
                    new PetPageController(petService, currentUserService))
            .build();

    @Test
    void showPetsShouldLoadPetsOfLoggedInOwner() {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(true);

        PetResponse pet = new PetResponse(
                10L, "Mochi", PetType.DOG, "Shiba", 2, 12.5,
                "MALE", null, null, null, 7L, "Test Owner", true,null
        );
        List<PetResponse> pets = List.of(pet);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);
        when(petService.getPetsByOwner(7L)).thenReturn(pets);

        ExtendedModelMap model = new ExtendedModelMap();
        PetPageController controller =
                new PetPageController(petService, currentUserService);

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

        verifyNoInteractions(currentUserService, petService);
    }

    @Test
    void showPetsShouldRejectUnknownOwner() throws Exception {
        when(currentUserService.getByEmail("owner@example.com"))
                .thenThrow(new ResourceNotFoundException(
                        "User", "owner@example.com"));

        mockMvc.perform(get("/pets")
                .principal(() -> "owner@example.com"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(petService);
    }

    @Test
    void showPetsShouldRejectInactiveOwner() throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(false);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);

        mockMvc.perform(get("/pets")
                .principal(() -> "owner@example.com"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(petService);
    }

    @Test
    void createPetShouldUseLoggedInOwner() throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(true);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);

        mockMvc.perform(post("/pets")
                .principal(() -> "owner@example.com")
                .param("name", "Mochi")
                .param("type", "DOG")
                .param("age", "2")
                .param("weight", "12.5")
                .param("breed", "Shiba"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pets"))
                .andExpect(flash().attribute(
                        "message", "เพิ่มสัตว์เลี้ยงสำเร็จ"));

        ArgumentCaptor<CreatePetRequest> requestCaptor =
                ArgumentCaptor.forClass(CreatePetRequest.class);

        verify(petService).createPet(eq(7L), requestCaptor.capture());
        assertEquals("Mochi", requestCaptor.getValue().name());
        assertEquals(PetType.DOG, requestCaptor.getValue().type());
        assertEquals(Integer.valueOf(2), requestCaptor.getValue().age());
    }

    @Test
    void createPetShouldRejectInvalidName() throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(true);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);

        mockMvc.perform(post("/pets")
                .principal(() -> "owner@example.com")
                .param("name", "")
                .param("type", "DOG"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pets"))
                .andExpect(flash().attribute(
                        "error", "กรุณาตรวจสอบข้อมูลสัตว์เลี้ยง"));

        verifyNoInteractions(petService);
    }

    @Test
    void createPetShouldRejectMissingLogin() throws Exception {
        mockMvc.perform(post("/pets")
                .param("name", "Mochi")
                .param("type", "DOG"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(currentUserService, petService);
    }

    @Test
    void updatePetShouldUseLoggedInOwner() throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(true);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);

        mockMvc.perform(post("/pets/10/edit")
                .principal(() -> "owner@example.com")
                .param("name", "Lily")
                .param("type", "CAT")
                .param("breed", "British Shorthair")
                .param("age", "4")
                .param("weight", "4.2")
                .param("gender", "FEMALE"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pets"))
                .andExpect(flash().attribute(
                        "editMessage", "แก้ไขสัตว์เลี้ยงสำเร็จ"));

        ArgumentCaptor<UpdatePetRequest> requestCaptor =
                ArgumentCaptor.forClass(UpdatePetRequest.class);

        verify(petService).updatePet(
                eq(10L), eq(7L), requestCaptor.capture());

        assertEquals("Lily", requestCaptor.getValue().name());
        assertEquals(PetType.CAT, requestCaptor.getValue().type());
        assertEquals(Integer.valueOf(4), requestCaptor.getValue().age());
        assertEquals(Double.valueOf(4.2), requestCaptor.getValue().weight());
    }

    @Test
    void updatePetShouldRejectInvalidRequest() throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(true);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);

        mockMvc.perform(post("/pets/10/edit")
                .principal(() -> "owner@example.com")
                .param("name", "")
                .param("type", "DOG"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pets"))
                .andExpect(flash().attribute(
                        "editError", "กรุณาตรวจสอบข้อมูลสัตว์เลี้ยง"))
                .andExpect(flash().attribute("editPetId", 10L));

        verifyNoInteractions(petService);
    }

    @Test
    void updatePetShouldRejectMissingLogin() throws Exception {
        mockMvc.perform(post("/pets/10/edit")
                .param("name", "Mochi")
                .param("type", "DOG"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(currentUserService, petService);
    }

    @Test
    void updatePetShouldReturn404ForAnotherOwnersPet() throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(true);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);

        when(petService.updatePet(
                eq(10L), eq(7L), any(UpdatePetRequest.class)))
                .thenThrow(new ResourceNotFoundException("Pet", 10L));

        mockMvc.perform(post("/pets/10/edit")
                .principal(() -> "owner@example.com")
                .param("name", "Mochi")
                .param("type", "DOG"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePetShouldUseLoggedInOwner() throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(true);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);

        mockMvc.perform(post("/pets/10/delete")
                .principal(() -> "owner@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/pets"))
                .andExpect(flash().attribute(
                        "deleteMessage", "นำสัตว์เลี้ยงออกจากรายการแล้ว"));

        verify(petService).deactivatePet(10L, 7L);
    }

    @Test
    void deletePetShouldRejectMissingLogin() throws Exception {
        mockMvc.perform(post("/pets/10/delete"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(currentUserService, petService);
    }

    @Test
    void deletePetShouldRejectInactiveOwner() throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(false);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);

        mockMvc.perform(post("/pets/10/delete")
                .principal(() -> "owner@example.com"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(petService);
    }

    @Test
    void deletePetShouldReturn404ForAnotherOwnersPet() throws Exception {
        User owner = new User();
        owner.setId(7L);
        owner.setActive(true);

        when(currentUserService.getByEmail("owner@example.com"))
                .thenReturn(owner);

        doThrow(new ResourceNotFoundException("Pet", 10L))
                .when(petService).deactivatePet(10L, 7L);

        mockMvc.perform(post("/pets/10/delete")
                .principal(() -> "owner@example.com"))
                .andExpect(status().isNotFound());
    }
}