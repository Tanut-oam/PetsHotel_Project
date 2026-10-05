package com.example.petshotel.controller.api;

import com.example.petshotel.domain.entity.User;
import com.example.petshotel.domain.enums.UserRole;
import com.example.petshotel.dto.request.CreateBookingApiRequest;
import com.example.petshotel.dto.request.CreateBookingRequest;
import com.example.petshotel.dto.response.BookingResponse;
import com.example.petshotel.dto.response.BookingPriceResponse;
import com.example.petshotel.dto.response.PetAvailabilityResponse;
import com.example.petshotel.service.BookingService;
import com.example.petshotel.service.CurrentUserService;
import com.example.petshotel.exception.ResourceNotFoundException;
import com.example.petshotel.exception.ErrorResponse;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Bookings",
        description = "การจองห้องพัก: สร้าง ดู ยกเลิก และเปลี่ยนสถานะ "
                + "(PENDING → CONFIRMED → CHECKED_IN → CHECKED_OUT, ยกเลิกได้ตอน PENDING/CONFIRMED)")
@RequiredArgsConstructor
public class BookingRestController {

    private final BookingService bookingService;
    private final CurrentUserService currentUserService;

    @PostMapping
    @Operation(summary = "สร้างการจอง",
            description = "ต้องล็อกอิน · จองให้ผู้ใช้ที่ล็อกอินอยู่ · การจองใหม่มีสถานะ PENDING "
                    + "· ระบบตรวจห้องว่าง ความจุ และสัตว์ที่ติดจองช่วงเดียวกัน")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "สร้างการจองสำเร็จ"),
            @ApiResponse(responseCode = "400", description = "ข้อมูลไม่ถูกต้อง เช่น วันที่ผิด สัตว์เกินความจุ หรือโปรโมชันหมดอายุ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "ยังไม่ล็อกอิน", content = @Content),
            @ApiResponse(responseCode = "403", description = "บัญชีถูกระงับ", content = @Content),
            @ApiResponse(responseCode = "404", description = "ไม่พบห้อง สัตว์ หรือโปรโมชัน",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "ห้องไม่ว่าง หรือสัตว์มีการจองซ้อนในช่วงเดียวกัน",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingApiRequest request,
            Principal principal) {

        User user = currentUser(principal);

        CreateBookingRequest serviceRequest = CreateBookingRequest.builder()
                .userId(user.getId())
                .roomId(request.roomId())
                .petIds(request.petIds())
                .checkInDate(request.checkInDate())
                .checkOutDate(request.checkOutDate())
                .servicePetIds(request.servicePetIds())
                .promotionId(request.promotionId())
                .build();

        BookingResponse response =
                bookingService.createBooking(serviceRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/preview-price")
    @Operation(summary = "คำนวณราคาก่อนจอง",
            description = "ต้องล็อกอิน · คำนวณราคาจากข้อมูลเดียวกับการจอง แต่ไม่บันทึกและไม่กันห้อง")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "คำนวณสำเร็จ"),
            @ApiResponse(responseCode = "400", description = "ข้อมูลไม่ถูกต้อง",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "ยังไม่ล็อกอิน", content = @Content),
            @ApiResponse(responseCode = "404", description = "ไม่พบห้อง สัตว์ หรือโปรโมชัน",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public BookingPriceResponse previewPrice(
            @Valid @RequestBody CreateBookingApiRequest request,
            Principal principal) {

        User user = currentUser(principal);

        return bookingService.previewPrice(CreateBookingRequest.builder()
                .userId(user.getId())
                .roomId(request.roomId())
                .petIds(request.petIds())
                .checkInDate(request.checkInDate())
                .checkOutDate(request.checkOutDate())
                .servicePetIds(request.servicePetIds())
                .promotionId(request.promotionId())
                .build());
    }

    @GetMapping("/pet-availability")
    @Operation(summary = "ดูสัตว์ที่ติดจองในช่วงวันที่",
            description = "ต้องล็อกอิน · คืนรหัสสัตว์ของผู้ใช้ที่มีการจองซ้อนในช่วงนี้ (ใช้ปิดตัวเลือกในฟอร์มจอง)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "สำเร็จ"),
            @ApiResponse(responseCode = "400", description = "วันที่ไม่ถูกต้อง หรือไม่ได้ระบุ",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "ยังไม่ล็อกอิน", content = @Content)
    })
    public PetAvailabilityResponse getPetAvailability(
            @Parameter(description = "วันเช็กอิน (yyyy-MM-dd)", example = "2026-11-01")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkInDate,

            @Parameter(description = "วันเช็กเอาต์ (yyyy-MM-dd)", example = "2026-11-03")
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkOutDate,

            Principal principal) {

                User user = currentUser(principal);

                return bookingService.getPetAvailability(
                        user.getId(),
                        checkInDate,
                        checkOutDate
                );
            }

    @GetMapping
    @Operation(summary = "ดูการจองทั้งหมด", description = "เฉพาะ STAFF / ADMIN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "สำเร็จ"),
            @ApiResponse(responseCode = "401", description = "ยังไม่ล็อกอิน", content = @Content),
            @ApiResponse(responseCode = "403", description = "ไม่ใช่ STAFF หรือ ADMIN", content = @Content)
    })
    public List<BookingResponse> getAllBookings(Principal principal) {
        requireStaffOrAdmin(currentUser(principal));

        return bookingService.getAllBookings();
    }

    @GetMapping("/{id}")
    @Operation(summary = "ดูรายละเอียดการจอง",
            description = "เจ้าของการจองดูได้เฉพาะของตัวเอง · STAFF / ADMIN ดูได้ทุกรายการ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "สำเร็จ"),
            @ApiResponse(responseCode = "401", description = "ยังไม่ล็อกอิน", content = @Content),
            @ApiResponse(responseCode = "403", description = "ไม่ใช่การจองของตัวเอง", content = @Content),
            @ApiResponse(responseCode = "404", description = "ไม่พบการจอง",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public BookingResponse getBookingById(
        @Parameter(description = "รหัสการจอง", example = "1")
            @PathVariable("id") Long id,
            Principal principal) {

        User user = currentUser(principal);
        BookingResponse booking = bookingService.getBookingById(id);

        requireOwnerOrStaff(user, booking);

        return booking;
    }

    @PostMapping("/{id}/confirm")
    // ⑥ confirmBooking
    @Operation(summary = "ยืนยันการจอง",
            description = "เฉพาะ STAFF / ADMIN · PENDING → CONFIRMED และส่งอีเมลแจ้งลูกค้า")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "เปลี่ยนสถานะสำเร็จ"),
            @ApiResponse(responseCode = "401", description = "ยังไม่ล็อกอิน", content = @Content),
            @ApiResponse(responseCode = "403", description = "ไม่ใช่ STAFF หรือ ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "ไม่พบการจอง",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "สถานะหรือวันที่ปัจจุบันไม่อนุญาตให้ทำรายการนี้",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public BookingResponse confirmBooking(
        @Parameter(description = "รหัสการจอง", example = "1")
            @PathVariable("id") Long id,
            Principal principal) {

        requireStaffOrAdmin(currentUser(principal));

        return bookingService.confirmBooking(id);
    }

    @PostMapping("/{id}/check-in")
    // ⑦ checkIn
    @Operation(summary = "เช็กอิน",
            description = "เฉพาะ STAFF / ADMIN · CONFIRMED → CHECKED_IN · ทำได้ตั้งแต่วันเช็กอินจนถึงก่อนวันเช็กเอาต์")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "เปลี่ยนสถานะสำเร็จ"),
            @ApiResponse(responseCode = "401", description = "ยังไม่ล็อกอิน", content = @Content),
            @ApiResponse(responseCode = "403", description = "ไม่ใช่ STAFF หรือ ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "ไม่พบการจอง",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "สถานะหรือวันที่ปัจจุบันไม่อนุญาตให้ทำรายการนี้",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public BookingResponse checkIn(
        @Parameter(description = "รหัสการจอง", example = "1")
            @PathVariable("id") Long id,
            Principal principal) {

        requireStaffOrAdmin(currentUser(principal));

        return bookingService.checkIn(id);
    }

    // ⑧ checkOut
    @PostMapping("/{id}/check-out")
    @Operation(summary = "เช็กเอาต์",
            description = "เฉพาะ STAFF / ADMIN · CHECKED_IN → CHECKED_OUT · ทำได้ตั้งแต่วันเช็กเอาต์")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "เปลี่ยนสถานะสำเร็จ"),
            @ApiResponse(responseCode = "401", description = "ยังไม่ล็อกอิน", content = @Content),
            @ApiResponse(responseCode = "403", description = "ไม่ใช่ STAFF หรือ ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "ไม่พบการจอง",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "สถานะหรือวันที่ปัจจุบันไม่อนุญาตให้ทำรายการนี้",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public BookingResponse checkOut(
        @Parameter(description = "รหัสการจอง", example = "1")
            @PathVariable("id") Long id,
            Principal principal) {

        requireStaffOrAdmin(currentUser(principal));

        return bookingService.checkOut(id);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "ยกเลิกการจอง",
            description = "เจ้าของหรือ STAFF / ADMIN · ยกเลิกได้เมื่อเป็น PENDING หรือ CONFIRMED และยังไม่ชำระเงิน")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "ยกเลิกสำเร็จ"),
            @ApiResponse(responseCode = "401", description = "ยังไม่ล็อกอิน", content = @Content),
            @ApiResponse(responseCode = "403", description = "ไม่ใช่การจองของตัวเอง", content = @Content),
            @ApiResponse(responseCode = "404", description = "ไม่พบการจอง",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "ยกเลิกไม่ได้ เช่น เช็กอินแล้ว หรือชำระเงินแล้ว",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public BookingResponse cancelBooking(
        @Parameter(description = "รหัสการจอง", example = "1")
            @PathVariable("id") Long id,
            Principal principal) {

        User user = currentUser(principal);
        BookingResponse booking = bookingService.getBookingById(id);

        requireOwnerOrStaff(user, booking);

        return bookingService.cancelBooking(id);
    }

    private User currentUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Login is required"
            );
        }

        User user;

        try {
            user = currentUserService.getByEmail(principal.getName());
        } catch (ResourceNotFoundException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user was not found"
            );
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new AccessDeniedException("Account is inactive");
        }

        return user;
    }

    private boolean isStaffOrAdmin(User user) {
        return user.getRole() == UserRole.STAFF
                || user.getRole() == UserRole.ADMIN;
    }

    private void requireStaffOrAdmin(User user) {
        if (!isStaffOrAdmin(user)) {
            throw new AccessDeniedException(
                    "This operation requires STAFF or ADMIN"
            );
        }
    }

    private void requireOwnerOrStaff(
            User user,
            BookingResponse booking) {

        boolean owner = Objects.equals(
                user.getId(),
                booking.getUserId()
        );

        if (!owner && !isStaffOrAdmin(user)) {
            throw new AccessDeniedException(
                    "You cannot access another customer's booking"
            );
        }
    }
}
