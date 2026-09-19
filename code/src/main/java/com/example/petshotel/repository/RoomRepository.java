package com.example.petshotel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.petshotel.domain.entity.Room;
import com.example.petshotel.domain.enums.RoomStatus;


public interface RoomRepository extends JpaRepository<Room,Long>{

    boolean existsByRoomNumber(String roomNumber);

    List<Room> findByStatus(RoomStatus status);
}