package com.example.petshotel.domain.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "booking_extra_services")
public class BookingExtraService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "extra_service_id", nullable = false)
    private ExtraService extraService;

    // Nullable for historical rows that never recorded a recipient.
    // Every new selection is linked to a BookingPet by BookingServiceImpl.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_pet_id")
    private BookingPet bookingPet;

    // New rows always have quantity 1. Retained for historical rows.
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
}
