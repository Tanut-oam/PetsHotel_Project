package com.example.petshotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingPet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;
}