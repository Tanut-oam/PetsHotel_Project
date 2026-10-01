package com.example.petshotel.dto.request;

import com.example.petshotel.domain.enums.RoomStatus;

import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
    @NotNull (message = "Please specify the room status.")
    RoomStatus status
) {}
