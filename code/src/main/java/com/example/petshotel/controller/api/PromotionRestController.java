package com.example.petshotel.controller.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.petshotel.dto.request.CreatePromotionRequest;
import com.example.petshotel.dto.response.PromotionResponse;
import com.example.petshotel.mapper.PromotionMapper;
import com.example.petshotel.service.PromotionService;

import jakarta.validation.Valid;

import com.example.petshotel.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/promotions")
@Tag(
    name = "Promotions",
    description = "API สำหรับจัดการโปรโมชัน ต้องเข้าสู่ระบบก่อนใช้งาน "
        + "การเพิ่ม แก้ไข และปิดใช้โปรโมชันต้องใช้สิทธิ์ ADMIN"
)
public class PromotionRestController {
    private final PromotionService promotionService;
    private final PromotionMapper promotionMapper;

    public PromotionRestController(
            PromotionService promotionService,
            PromotionMapper promotionMapper){
        this.promotionService = promotionService;
        this.promotionMapper = promotionMapper;
    }

    @Operation(
        summary = "ดูโปรโมชันทั้งหมด",
        description = "แสดงโปรโมชันทั้งหมด รวมรายการที่ปิดใช้ "
            + "ยังไม่เริ่มใช้งาน และหมดอายุแล้ว"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "แสดงรายการโปรโมชันสำเร็จ ถ้าไม่มีข้อมูลจะคืนรายการว่าง",
            useReturnTypeSchema = true
        ),
        @ApiResponse(
            responseCode = "401",
            description = "ยังไม่ได้เข้าสู่ระบบ",
            content = @Content
        )
    })
    @GetMapping
    public List<PromotionResponse> getAllPromotions(){
        return promotionService.getAllPromotions().stream()
                .map(promotionMapper::toResponse).toList();
    }


    @Operation(
        summary = "ดูโปรโมชันที่ใช้งานได้ในวันนี้",
        description = "แสดงเฉพาะโปรโมชันที่เปิดใช้ "
            + "และวันนี้อยู่ระหว่างวันเริ่มต้นกับวันสิ้นสุด "
            + "โดยรวมวันเริ่มต้นและวันสิ้นสุดด้วย"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "แสดงโปรโมชันที่ใช้งานได้สำเร็จ",
            useReturnTypeSchema = true
        ),
        @ApiResponse(
            responseCode = "401",
            description = "ยังไม่ได้เข้าสู่ระบบ",
            content = @Content
        )
    })
    @GetMapping("/active")
    public List<PromotionResponse> getActivePromotions(){
        return promotionService.getActivePromotions().stream()
                .map(promotionMapper::toResponse).toList();
    }

    @Operation(
        summary = "ดูโปรโมชันตามรหัส",
        description = "ค้นหาโปรโมชันหนึ่งรายการด้วย id "
            + "สามารถดูรายการที่ปิดใช้หรือหมดอายุแล้วได้"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "พบโปรโมชัน",
            useReturnTypeSchema = true
        ),
        @ApiResponse(
            responseCode = "400",
            description = "รหัสโปรโมชันมีรูปแบบไม่ถูกต้อง",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "ยังไม่ได้เข้าสู่ระบบ",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "ไม่พบโปรโมชันตามรหัสที่ระบุ",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/{id}")
    public PromotionResponse getPromotionById(@PathVariable Long id){
        return promotionMapper.toResponse(
            promotionService.getPromotionById(id));
    }

    @Operation(
        summary = "เพิ่มโปรโมชัน",
        description = "สำหรับ ADMIN สร้างโปรโมชันใหม่ "
            + "ส่งวันที่เป็น yyyy-MM-dd "
            + "วันสิ้นสุดต้องไม่ก่อนวันเริ่มต้น "
            + "และสามารถเป็นวันเดียวกันได้"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "สร้างโปรโมชันสำเร็จ",
            useReturnTypeSchema = true
        ),
        @ApiResponse(
            responseCode = "400",
            description = "ข้อมูลไม่ถูกต้อง เช่น ไม่ระบุชื่อ "
                + "ส่วนลดไม่เป็นบวก เปอร์เซ็นต์เกิน 100 "
                + "หรือวันสิ้นสุดก่อนวันเริ่มต้น",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "ยังไม่ได้เข้าสู่ระบบ",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "ไม่มีสิทธิ์ ADMIN หรือ CSRF token ไม่ถูกต้อง",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<PromotionResponse> createPromotion(@Valid @RequestBody CreatePromotionRequest request){
        PromotionResponse response = promotionMapper.toResponse(
            promotionService.createPromotion(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "แก้ไขโปรโมชัน",
        description = "สำหรับ ADMIN แก้ไขโปรโมชันตาม id "
            + "โดยส่งข้อมูลทุกช่องตาม CreatePromotionRequest "
            + "รวมถึงสถานะเปิดใช้งาน"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "แก้ไขโปรโมชันสำเร็จ",
            useReturnTypeSchema = true
        ),
        @ApiResponse(
            responseCode = "400",
            description = "รหัสหรือข้อมูลโปรโมชันไม่ถูกต้อง",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "ยังไม่ได้เข้าสู่ระบบ",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "ไม่มีสิทธิ์ ADMIN หรือ CSRF token ไม่ถูกต้อง",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "ไม่พบโปรโมชันที่ต้องการแก้ไข",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PutMapping("/{id}")
    public PromotionResponse updatePromotion(@PathVariable Long id, @Valid @RequestBody CreatePromotionRequest request){
        return promotionMapper.toResponse(
            promotionService.updatePromotion(id, request));
    }

    @Operation(
        summary = "ปิดใช้โปรโมชัน",
        description = "สำหรับ ADMIN เปลี่ยนสถานะ active เป็น false "
            + "โดยยังเก็บข้อมูลโปรโมชันไว้ในฐานข้อมูล "
            + "เมื่อสำเร็จจะไม่มีข้อมูลใน response body"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "ปิดใช้โปรโมชันสำเร็จ",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "รหัสโปรโมชันมีรูปแบบไม่ถูกต้อง",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "ยังไม่ได้เข้าสู่ระบบ",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "ไม่มีสิทธิ์ ADMIN หรือ CSRF token ไม่ถูกต้อง",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "ไม่พบโปรโมชันที่ต้องการปิดใช้",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivatePromotion(@PathVariable Long id){
        promotionService.deactivatePromotion(id);
        return ResponseEntity.noContent().build();
    }

}
