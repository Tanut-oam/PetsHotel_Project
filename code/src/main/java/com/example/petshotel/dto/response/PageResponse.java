package com.example.petshotel.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ผลลัพธ์แบบแบ่งหน้า")
public record PageResponse<T>(
        @Schema(description = "ข้อมูลในหน้านี้") List<T> content,
        @Schema(description = "หน้าปัจจุบัน (เริ่มที่ 0)", example = "0") int page,
        @Schema(description = "จำนวนรายการต่อหน้า", example = "10") int size,
        @Schema(description = "จำนวนรายการทั้งหมด", example = "23") long totalElements,
        @Schema(description = "จำนวนหน้าทั้งหมด", example = "3") int totalPages,
        @Schema(description = "เป็นหน้าแรกหรือไม่", example = "true") boolean first,
        @Schema(description = "เป็นหน้าสุดท้ายหรือไม่", example = "false") boolean last,
        @Schema(description = "การเรียงลำดับที่ใช้", example = "pricePerPetPerNight: ASC") String sort) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.getSort().toString());
    }
}