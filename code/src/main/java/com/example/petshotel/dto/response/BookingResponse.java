package com.example.petshotel.dto.response;

import com.example.petshotel.domain.enums.BookingStatus;
import com.example.petshotel.domain.enums.PaymentStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "รายละเอียดการจอง ห้องพัก สัตว์เลี้ยง ราคา และสถานะปัจจุบัน")
public class BookingResponse {

    @Schema(
            description = "รหัสการจอง",
            example = "1"
    )
    private Long id;

    @Schema(
            description = "รหัสผู้ใช้ที่เป็นเจ้าของการจอง",
            example = "10"
    )
    private Long userId;

    @Schema(
            description = "รหัสห้องพักที่จอง",
            example = "3"
    )
    private Long roomId;

    @Schema(
            description = "รหัสสัตว์เลี้ยงที่เข้าพักในการจองนี้",
            example = "[5, 6]"
    )
    private List<Long> petIds;

    @Schema(
            description = "วันเช็กอิน รูปแบบ yyyy-MM-dd",
            example = "2026-11-01"
    )
    private LocalDate checkInDate;

    @Schema(
            description = "วันเช็กเอาต์ รูปแบบ yyyy-MM-dd",
            example = "2026-11-03"
    )
    private LocalDate checkOutDate;

    @Schema(
            description = "สถานะปัจจุบันของการจอง",
            example = "CONFIRMED",
            allowableValues = {
                    "PENDING",
                    "CONFIRMED",
                    "CHECKED_IN",
                    "CHECKED_OUT",
                    "CANCELLED"
            }
    )
    private BookingStatus status;

    @Schema(
            description = "ยอดรวมสุทธิของการจองหลังหักส่วนลด หน่วยเป็นบาท",
            example = "1170.00"
    )
    private BigDecimal totalPrice;

    @Schema(
            description = "สถานะการชำระเงิน",
            example = "UNPAID",
            allowableValues = {
                    "UNPAID",
                    "PAID"
            }
    )
    private PaymentStatus paymentStatus;

    @Schema(
            description = "ระบุว่าสามารถเช็กอินได้ในวันที่ปัจจุบันหรือไม่",
            example = "true"
    )
    private Boolean checkInAllowed;

    @Schema(
            description = "ระบุว่าสามารถเช็กเอาต์ได้ในวันที่ปัจจุบันหรือไม่",
            example = "false"
    )
    private Boolean checkOutAllowed;

    @Schema(
            description = "ชื่อห้องพัก ณ เวลาที่แสดงผล",
            example = "ห้องดีลักซ์"
    )
    private String roomName;

    @Schema(
            description = "รายชื่อสัตว์เลี้ยงที่เข้าพักในการจองนี้",
            example = "[\"โมจิ\", \"โกโก้\"]"
    )
    private List<String> petNames;

    @Schema(
            description = "ชื่อโปรโมชันที่ใช้ตอนจอง ถ้าไม่ได้ใช้โปรโมชันจะเป็น null",
            example = "SAVE10"
    )
    private String promotionName;

    @Schema(
            description = "ค่าห้องพักก่อนรวมบริการเสริมและส่วนลด หน่วยเป็นบาท",
            example = "1000.00"
    )
    private BigDecimal roomAmount;

    @Schema(
            description = "ค่าบริการเสริมทั้งหมด หน่วยเป็นบาท",
            example = "300.00"
    )
    private BigDecimal serviceAmount;

    @Schema(
            description = "ค่าธรรมเนียมหรือค่าบวกเพิ่มเติม เช่น ค่าบริการช่วงวันหยุด หน่วยเป็นบาท",
            example = "0.00"
    )
    private BigDecimal surchargeAmount;

    @Schema(
            description = "ส่วนลดจากโปรโมชัน หน่วยเป็นบาท",
            example = "130.00"
    )
    private BigDecimal discountAmount;

    @Schema(
            description = "รายการบริการเสริมที่เลือก พร้อมสัตว์ผู้รับบริการ"
    )
    private List<ExtraServiceLine> extraServices;

    @Schema(
            description = "รายละเอียดบริการเสริมหนึ่งรายการในการจอง"
    )
    public record ExtraServiceLine(

            @Schema(
                    description = "ชื่อบริการเสริม",
                    example = "อาบน้ำ"
            )
            String name,

            @Schema(
                    description = "จำนวนหน่วยของบริการ",
                    example = "1"
            )
            Integer quantity,

            @Schema(
                    description = "ราคาต่อหน่วย หน่วยเป็นบาท",
                    example = "300.00"
            )
            BigDecimal unitPrice,

            @Schema(
                    description = "ราคารวมของบริการรายการนี้ หน่วยเป็นบาท",
                    example = "300.00"
            )
            BigDecimal totalPrice,

            @Schema(
                    description = "รหัสสัตว์เลี้ยงที่รับบริการ",
                    example = "5"
            )
            Long petId,

            @Schema(
                    description = "ชื่อสัตว์เลี้ยงที่รับบริการ",
                    example = "โมจิ"
            )
            String petName
    ) {
    }
}