package com.example.petshotel.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ข้อมูลบริการเสริมที่ API ตอบกลับ")
public record ExtraServiceResponse(

    @Schema(
        description = "รหัสบริการเสริม",
        example = "1"
    )
    Long id,

    @Schema(
        description = "ชื่อบริการเสริม",
        example = "อาบน้ำ"
    )
    String name,

    @Schema(
        description = "รายละเอียดบริการเสริม "
            + "อาจเป็น null หากไม่ได้กำหนด",
        example = "บริการอาบน้ำและเป่าขนสัตว์เลี้ยง"
    )
    String description,

    @Schema(
        description = "ราคาบริการเสริม หน่วยเป็นบาท",
        example = "250.00"
    )
    BigDecimal price,

    @Schema(
        description = "สถานะเปิดใช้งาน: true คือเปิดใช้ false คือปิดใช้",
        example = "true"
    )
    Boolean active

) {}