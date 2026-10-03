package com.example.petshotel.controller.api;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petshotel.dto.request.RoomSearchRequest;
import com.example.petshotel.dto.response.PageResponse;
import com.example.petshotel.dto.response.RoomResponse;
import com.example.petshotel.exception.ErrorResponse;
import com.example.petshotel.service.RoomSearchService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Room Search", description = "ค้นหาห้องพักแบบแบ่งหน้าและเรียงลำดับ (ไม่ต้องล็อกอิน)")
@RestController
@RequestMapping("/api/rooms")
public class RoomSearchRestController {

    private final RoomSearchService roomSearchService;

    public RoomSearchRestController(RoomSearchService roomSearchService) {
        this.roomSearchService = roomSearchService;
    }

    @Operation(
            summary = "ค้นหาห้องพัก (Pagination & Sorting)",
            description = "ค้นหาห้องที่เปิดให้จองตามคำค้น จำนวนสัตว์ขั้นต่ำ และราคาสูงสุด "
                    + "แบ่งหน้าด้วย page (เริ่ม 0) และ size (สูงสุด 50) "
                    + "เรียงด้วย sort=field,asc|desc โดย field ได้แก่ roomNumber, name, capacity, pricePerPetPerNight")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ผลการค้นหาแบบแบ่งหน้า"),
            @ApiResponse(responseCode = "400", description = "เงื่อนไขไม่ถูกต้อง, size เกิน 50 หรือ field ที่ใช้เรียงไม่รองรับ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search")
    public PageResponse<RoomResponse> searchRooms(
            @Valid @ParameterObject @ModelAttribute RoomSearchRequest request,
            @ParameterObject
            @PageableDefault(size = 10, sort = "roomNumber", direction = Sort.Direction.ASC)
            Pageable pageable) {

        return roomSearchService.searchRooms(request, pageable);
    }
}