package com.example.petshotel.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.enums.RoomStatus;

import jakarta.persistence.LockModeType;

public interface RoomRepository extends JpaRepository<Room,Long>{

    boolean existsByRoomNumber(String roomNumber);

    List<Room> findByStatus(RoomStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :roomId")
    Optional<Room> findByIdForUpdate(@Param("roomId") Long roomId);
        // ค้นหาห้องแบบแบ่งหน้า: Pageable ใส่ LIMIT/OFFSET และ ORDER BY ให้อัตโนมัติ
    @Query("""
            SELECT r FROM Room r
            WHERE r.status = :status
              AND (LOWER(r.name) LIKE :keyword OR LOWER(r.roomNumber) LIKE :keyword)
              AND r.capacity >= :minCapacity
              AND r.pricePerPetPerNight <= :maxPrice
            """)
    Page<Room> searchRooms(@Param("status") RoomStatus status,
                           @Param("keyword") String keyword,
                           @Param("minCapacity") int minCapacity,
                           @Param("maxPrice") BigDecimal maxPrice,
                           Pageable pageable);
}