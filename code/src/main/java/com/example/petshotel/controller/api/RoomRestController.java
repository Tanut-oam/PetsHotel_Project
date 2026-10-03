package com.example.petshotel.controller.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petshotel.dto.request.CreateRoomRequest;
import com.example.petshotel.dto.request.UpdateRoomRequest;
import com.example.petshotel.dto.request.UpdateStatusRequest;
import com.example.petshotel.dto.response.RoomResponse;
import com.example.petshotel.exception.ErrorResponse;
import com.example.petshotel.service.RoomService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Rooms", description = "จัดการห้องพัก: ดูรายการ เพิ่ม แก้ไข และเปลี่ยนสถานะห้อง")
public class RoomRestController {

    private final RoomService roomService;

    public RoomRestController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping("")
    @Operation(summary = "ดูห้องทั้งหมด",
            description = "คืนห้องทุกสถานะ (ACTIVE / INACTIVE / MAINTENANCE) ไม่ต้องล็อกอิน")
    @ApiResponse(responseCode = "200", description = "สำเร็จ")
    public List<RoomResponse> getAllRooms() {
        return roomService.getAllRooms();
    }

    @GetMapping("/{id}")
    @Operation(summary = "ดูรายละเอียดห้อง", description = "ไม่ต้องล็อกอิน")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "สำเร็จ"),
            @ApiResponse(responseCode = "404", description = "ไม่พบห้อง",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public RoomResponse getRoomById(
            @Parameter(description = "รหัสห้อง", example = "1") @PathVariable Long id) {
        return roomService.getRoomById(id);
    }

    @PostMapping("")
    @Operation(summary = "เพิ่มห้องใหม่",
            description = "เฉพาะ ADMIN · ห้องใหม่จะมีสถานะ ACTIVE เสมอ")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "สร้างห้องสำเร็จ"),
            @ApiResponse(responseCode = "400", description = "ข้อมูลไม่ถูกต้อง",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "ยังไม่ล็อกอิน", content = @Content),
            @ApiResponse(responseCode = "403", description = "ไม่ใช่ ADMIN", content = @Content),
            @ApiResponse(responseCode = "409", description = "เลขห้องซ้ำ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "แก้ไขข้อมูลห้อง",
            description = "เฉพาะ ADMIN · แก้เลขห้อง ชื่อ รายละเอียด ความจุ และราคา (ไม่เปลี่ยนสถานะ)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "แก้ไขสำเร็จ"),
            @ApiResponse(responseCode = "400", description = "ข้อมูลไม่ถูกต้อง",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "ยังไม่ล็อกอิน", content = @Content),
            @ApiResponse(responseCode = "403", description = "ไม่ใช่ ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "ไม่พบห้อง",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "เลขห้องซ้ำกับห้องอื่น",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public RoomResponse updateRoom(
            @Parameter(description = "รหัสห้อง", example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateRoomRequest request) {
        return roomService.updateRoom(id, request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "เปลี่ยนสถานะห้อง",
            description = "เฉพาะ ADMIN · ACTIVE = เปิดให้จอง, MAINTENANCE = ปิดซ่อม, INACTIVE = ปิดใช้งาน")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "เปลี่ยนสถานะสำเร็จ"),
            @ApiResponse(responseCode = "400", description = "ไม่ได้ระบุสถานะ หรือสถานะไม่ถูกต้อง",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "ยังไม่ล็อกอิน", content = @Content),
            @ApiResponse(responseCode = "403", description = "ไม่ใช่ ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "ไม่พบห้อง",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public RoomResponse setRoomStatus(
            @Parameter(description = "รหัสห้อง", example = "1") @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        return roomService.setRoomStatus(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "ปิดใช้งานห้อง",
            description = "เฉพาะ ADMIN · เปลี่ยนสถานะเป็น INACTIVE (ไม่ลบข้อมูลห้อง)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ปิดใช้งานสำเร็จ"),
            @ApiResponse(responseCode = "401", description = "ยังไม่ล็อกอิน", content = @Content),
            @ApiResponse(responseCode = "403", description = "ไม่ใช่ ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "ไม่พบห้อง",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public RoomResponse deactivateRoom(
            @Parameter(description = "รหัสห้อง", example = "1") @PathVariable Long id) {
        return roomService.deactivateRoom(id);
    }
}