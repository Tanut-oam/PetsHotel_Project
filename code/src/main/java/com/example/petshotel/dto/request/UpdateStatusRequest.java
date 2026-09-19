package com.example.petshotel.dto.request;

import com.example.petshotel.domain.enums.RoomStatus;

public record UpdateStatusRequest(RoomStatus status) {
    
}
