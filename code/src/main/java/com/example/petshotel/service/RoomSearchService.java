package com.example.petshotel.service;

import org.springframework.data.domain.Pageable;

import com.example.petshotel.dto.request.RoomSearchRequest;
import com.example.petshotel.dto.response.PageResponse;
import com.example.petshotel.dto.response.RoomResponse;

public interface RoomSearchService {

    PageResponse<RoomResponse> searchRooms(RoomSearchRequest request, Pageable pageable);
}