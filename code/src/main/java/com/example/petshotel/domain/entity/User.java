package com.example.petshotel.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.example.petshotel.domain.enums.UserRole;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor

@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;
    
    @Column(unique = true,nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private  String phoneNumber;

    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
