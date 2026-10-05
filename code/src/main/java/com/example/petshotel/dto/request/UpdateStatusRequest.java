package com.example.petshotel.dto.request;

import com.example.petshotel.domain.enums.RoomStatus;

import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "สถานะห้องที่ต้องการเปลี่ยน")
public record UpdateStatusRequest(
    @Schema(description = "ACTIVE = เปิดให้จอง, MAINTENANCE = ปิดซ่อม, INACTIVE = ปิดใช้งาน",
            example = "MAINTENANCE")
    @NotNull (message = "Please specify the room status.")
    RoomStatus status
) {}
