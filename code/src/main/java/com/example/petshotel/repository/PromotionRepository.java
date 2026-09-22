package com.example.petshotel.repository;

import com.example.petshotel.domain.entity.Promotion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

 
public interface PromotionRepository extends JpaRepository<Promotion, Long>{
    List<Promotion> findByActiveTrue();
}
