package com.example.petshotel.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
    name = "daily_care_reports",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_care_report_booking_pet_date",
        columnNames = {"booking_pet_id", "report_date"}
    )
)
public class DailyCareReport {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_pet_id", nullable = false)
    private BookingPet bookingPet;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn (name = "recorded_by_id",nullable = false)
    private User recordedBy;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    private String feedingMorning;
    private String feedingEvening;
    private Integer walkingMinutes;
    private String grooming;
    private String mood;
    private String healthNote;
    private String generalNote;

    @Column(nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @PrePersist 
    public void prePersist(){
        if(createdAt == null){
            createdAt = LocalDateTime.now();
        }
    }
}
