package com.example.petshotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.enums.RoomStatus;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface RoomRepository extends JpaRepository<Room,Long>{

    boolean existsByRoomNumber(String roomNumber);

    List<Room> findByStatus(RoomStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Room r WHERE r.id = :roomId")
    Optional<Room> findByIdForUpdate(@Param("roomId") Long roomId);
}