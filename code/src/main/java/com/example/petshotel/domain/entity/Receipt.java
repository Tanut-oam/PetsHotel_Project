package com.example.petshotel.domain.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity 
@NoArgsConstructor
@AllArgsConstructor
@Getter 
@Setter 
@Table(name = "receipts")
public class Receipt {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_number", nullable = false, unique = true)
    private String receiptNumber;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "room_amount", precision = 10, scale = 2)
    private BigDecimal roomAmount;

    @Column(name = "service_amount", precision = 10, scale = 2)
    private BigDecimal serviceAmount;

    @Column(name = "surcharge_amount", precision = 10, scale = 2)
    private BigDecimal surchargeAmount;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;
    
}
