package com.example.petshotel.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.petshotel.domain.entity.ExtraService;


public interface ExtraServiceRepository extends JpaRepository<ExtraService, Long>{
    List<ExtraService> findByActiveTrue();
}
