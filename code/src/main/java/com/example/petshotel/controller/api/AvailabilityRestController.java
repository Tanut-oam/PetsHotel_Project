package com.example.petshotel.controller.api;

import java.time.LocalDate;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.dto.request.AvailabilitySearchRequest;
import com.example.petshotel.dto.response.AvailabilityResponse;
import com.example.petshotel.dto.response.RoomAvailabilityCalendarResponse;
import com.example.petshotel.exception.ErrorResponse;
import com.example.petshotel.mapper.AvailabilityMapper;
import com.example.petshotel.service.AvailabilityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Availability", description = "ค้นหาห้องว่างและดูช่วงวันที่ห้องไม่ว่าง (ไม่ต้องล็อกอิน)")
@RestController
@RequestMapping("/api/rooms")
public class AvailabilityRestController {

    private final AvailabilityService availabilityService;
    private final AvailabilityMapper availabilityMapper;

    public AvailabilityRestController(
            AvailabilityService availabilityService,
            AvailabilityMapper availabilityMapper) {

        this.availabilityService = availabilityService;
        this.availabilityMapper = availabilityMapper;
    }

    @Operation(
            summary = "ค้นหาห้องว่าง",
            description = "คืนห้องที่เปิดให้จอง รับสัตว์ได้ตามจำนวน และไม่มีการจองซ้อนในช่วง checkIn ถึง checkOut "
                    + "(ห้องว่างทั้งห้อง ไม่แชร์กับการจองอื่น)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "รายการห้องว่าง (อาจเป็นรายการว่าง)"),
            @ApiResponse(responseCode = "400", description = "พารามิเตอร์ไม่ครบหรือไม่ถูกต้อง เช่น checkOut ไม่อยู่หลัง checkIn",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/available")
    public AvailabilityResponse findAvailableRooms(
            @Valid
            @ParameterObject
            @ModelAttribute
            AvailabilitySearchRequest request) {

        List<Room> rooms =
                availabilityService.findAvailableRooms(
                        request.checkIn(),
                        request.checkOut(),
                        request.petCount()
                );

        return availabilityMapper.toResponse(
                request,
                rooms
        );
    }

    @Operation(
            summary = "ดูช่วงวันที่ห้องไม่ว่าง",
            description = "คืนช่วงวันที่ที่ห้องถูกจองแล้วภายในช่วง fromDate ถึง toDate (ดูล่วงหน้าได้ไม่เกิน 1 ปี) "
                    + "ใช้แสดงปฏิทินในหน้ารายละเอียดห้อง")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ช่วงวันที่ห้องไม่ว่าง"),
            @ApiResponse(responseCode = "400", description = "ช่วงวันที่ไม่ถูกต้องหรือเกิน 1 ปี",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "ไม่พบห้อง",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{roomId}/availability-calendar")
    public RoomAvailabilityCalendarResponse
            getAvailabilityCalendar(
                    @Parameter(description = "รหัสห้อง", example = "1")
                    @PathVariable Long roomId,

                    @Parameter(description = "วันเริ่มต้นของปฏิทิน (yyyy-MM-dd)", example = "2026-10-01")
                    @RequestParam
                    @DateTimeFormat(
                            iso = DateTimeFormat.ISO.DATE
                    )
                    LocalDate fromDate,

                    @Parameter(description = "วันสิ้นสุดของปฏิทิน (yyyy-MM-dd)", example = "2026-10-31")
                    @RequestParam
                    @DateTimeFormat(
                            iso = DateTimeFormat.ISO.DATE
                    )
                    LocalDate toDate) {

        return availabilityService
                .getRoomAvailabilityCalendar(
                        roomId,
                        fromDate,
                        toDate
                );
    }
}