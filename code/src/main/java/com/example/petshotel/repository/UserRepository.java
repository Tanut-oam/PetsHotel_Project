package com.example.petshotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.petshotel.domain.entity.User;

public interface UserRepository extends JpaRepository<User,Long>{

    
}