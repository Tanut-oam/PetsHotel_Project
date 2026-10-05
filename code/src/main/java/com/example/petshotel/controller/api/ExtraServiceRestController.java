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

import com.example.petshotel.dto.request.ExtraServiceRequest;
import com.example.petshotel.dto.response.ExtraServiceResponse;
import com.example.petshotel.mapper.ExtraServiceMapper;
import com.example.petshotel.service.ExtraServiceService;

import jakarta.validation.Valid;

import com.example.petshotel.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/extra-services")
@Tag(
    name = "Extra Services",
    description = "API สำหรับจัดการบริการเสริม ต้องเข้าสู่ระบบก่อนใช้งาน "
        + "การเพิ่ม แก้ไข และปิดใช้บริการต้องใช้สิทธิ์ ADMIN"
)
public class ExtraServiceRestController {
    private final ExtraServiceService extraServiceService;
    private final ExtraServiceMapper extraServiceMapper;

    public ExtraServiceRestController(
        ExtraServiceService extraServiceService,
        ExtraServiceMapper extraServiceMapper){
            this.extraServiceService = extraServiceService;
            this.extraServiceMapper = extraServiceMapper;
    }

    @Operation(
        summary = "ดูบริการเสริมทั้งหมด",
        description = "แสดงบริการเสริมทั้งหมด รวมรายการที่ปิดใช้แล้ว"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "แสดงรายการบริการเสริมสำเร็จ "
                + "ถ้าไม่มีข้อมูลจะคืนรายการว่าง",
            useReturnTypeSchema = true
        ),
        @ApiResponse(
            responseCode = "401",
            description = "ยังไม่ได้เข้าสู่ระบบ",
            content = @Content
        )
    })
    @GetMapping
    public List<ExtraServiceResponse> getAllExtraServices(){
        return extraServiceService.getAllExtraServices().stream()
                .map(extraServiceMapper::toResponse).toList();
    }

    @Operation(
        summary = "ดูบริการเสริมตามรหัส",
        description = "ค้นหาบริการเสริมหนึ่งรายการด้วย id "
            + "สามารถดูรายการที่ปิดใช้แล้วได้"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "พบบริการเสริม",
            useReturnTypeSchema = true
        ),
        @ApiResponse(
            responseCode = "400",
            description = "รหัสบริการเสริมมีรูปแบบไม่ถูกต้อง",
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
            description = "ไม่พบบริการเสริมตามรหัสที่ระบุ",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/{id}")
    public ExtraServiceResponse getExtraServiceById(@PathVariable Long id){
        return extraServiceMapper.toResponse(extraServiceService.getExtraServiceById(id));
    }

    @Operation(
        summary = "เพิ่มบริการเสริม",
        description = "สำหรับ ADMIN สร้างบริการเสริมใหม่ "
            + "ราคาต้องไม่ติดลบ และรองรับทศนิยมไม่เกิน 2 ตำแหน่ง "
            + "หากไม่ส่ง active หรือส่งเป็น null จะเปิดใช้งานโดยอัตโนมัติ"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "สร้างบริการเสริมสำเร็จ",
            useReturnTypeSchema = true
        ),
        @ApiResponse(
            responseCode = "400",
            description = "ข้อมูลไม่ถูกต้อง เช่น ไม่ระบุชื่อ "
                + "ไม่ระบุราคา ราคาติดลบ หรือทศนิยมเกิน 2 ตำแหน่ง",
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
    public ResponseEntity<ExtraServiceResponse> createExtraService(
            @Valid @RequestBody ExtraServiceRequest request){
                ExtraServiceResponse response = extraServiceMapper.toResponse(
                    extraServiceService.createExtraService(
                        extraServiceMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
        summary = "แก้ไขบริการเสริม",
        description = "สำหรับ ADMIN แก้ไขบริการเสริมตาม id "
            + "ต้องส่งชื่อและราคา "
            + "หากไม่ระบุ active หรือส่งเป็น null "
            + "ระบบจะคงสถานะเปิดใช้งานเดิม"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "แก้ไขบริการเสริมสำเร็จ",
            useReturnTypeSchema = true
        ),
        @ApiResponse(
            responseCode = "400",
            description = "รหัสหรือข้อมูลบริการเสริมไม่ถูกต้อง",
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
            description = "ไม่พบบริการเสริมที่ต้องการแก้ไข",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PutMapping("/{id}")
    public ExtraServiceResponse updateExtraService(
        @PathVariable Long id,
        @Valid @RequestBody ExtraServiceRequest request){
            return extraServiceMapper.toResponse(
                extraServiceService.updateExtraService(id, extraServiceMapper.toEntity(request)));
    }

    @Operation(
        summary = "ปิดใช้บริการเสริม",
        description = "สำหรับ ADMIN เปลี่ยนสถานะ active เป็น false "
            + "โดยยังเก็บข้อมูลบริการเสริมไว้ในฐานข้อมูล "
            + "เมื่อสำเร็จจะไม่มีข้อมูลใน response body"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "ปิดใช้บริการเสริมสำเร็จ",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "รหัสบริการเสริมมีรูปแบบไม่ถูกต้อง",
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
            description = "ไม่พบบริการเสริมที่ต้องการปิดใช้",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExtraService(@PathVariable Long id){
        extraServiceService.deleteExtraService(id);
        return ResponseEntity.noContent().build();
    }


}
