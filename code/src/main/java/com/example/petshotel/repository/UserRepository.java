package com.example.petshotel.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.petshotel.domain.entity.User;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User,Long>{

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
            SELECT u FROM User u
            WHERE LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR u.phoneNumber LIKE CONCAT('%', :keyword, '%')
            """)
    List<User> search(@Param("keyword") String keyword, Sort sort);
}