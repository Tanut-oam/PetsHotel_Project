package com.example.petshotel.domain.entity;

import java.math.BigDecimal;

import com.example.petshotel.domain.enums.RoomStatus;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String roomNumber;
    
    private String name;
    private String description;
    private String capacity;

    @Column(nullable = false,precision = 10,scale = 2)  //เลข 10  หลัก ทศนิยม 2 หลัก
    private BigDecimal pricePerPetPerNight;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;

}
