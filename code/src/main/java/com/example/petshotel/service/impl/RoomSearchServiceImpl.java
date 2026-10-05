package com.example.petshotel.service.impl;

import java.math.BigDecimal;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.petshotel.domain.enums.RoomStatus;
import com.example.petshotel.dto.request.RoomSearchRequest;
import com.example.petshotel.dto.response.PageResponse;
import com.example.petshotel.dto.response.RoomResponse;
import com.example.petshotel.mapper.RoomMapper;
import com.example.petshotel.repository.RoomRepository;
import com.example.petshotel.service.RoomSearchService;

@Service
public class RoomSearchServiceImpl implements RoomSearchService {

    static final int MAX_PAGE_SIZE = 50;

    // field ที่อนุญาตให้เรียง ป้องกันผู้ใช้ส่งชื่อ field มั่ว ๆ แล้วเกิด error 500
    static final Set<String> SORTABLE_FIELDS =
            Set.of("roomNumber", "name", "capacity", "pricePerPetPerNight");

    // ค่าสูงสุดของคอลัมน์ราคา precision 10 scale 2
    private static final BigDecimal NO_PRICE_LIMIT = new BigDecimal("99999999.99");

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;

    public RoomSearchServiceImpl(RoomRepository roomRepository, RoomMapper roomMapper) {
        this.roomRepository = roomRepository;
        this.roomMapper = roomMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoomResponse> searchRooms(RoomSearchRequest request, Pageable pageable) {
        validatePageable(pageable);

        String keyword = request.keyword() == null ? "" : request.keyword().trim().toLowerCase();
        int minCapacity = request.minCapacity() == null ? 1 : request.minCapacity();
        BigDecimal maxPrice = request.maxPrice() == null ? NO_PRICE_LIMIT : request.maxPrice();

        return PageResponse.from(
                roomRepository.searchRooms(
                                RoomStatus.ACTIVE,
                                "%" + keyword + "%",
                                minCapacity,
                                maxPrice,
                                pageable)
                        .map(roomMapper::toResponse));
    }

    private void validatePageable(Pageable pageable) {
        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("แสดงได้สูงสุดหน้าละ " + MAX_PAGE_SIZE + " รายการ");
        }
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new IllegalArgumentException(
                        "ไม่สามารถเรียงตาม " + order.getProperty()
                                + " ได้ (เรียงได้ตาม " + String.join(", ", SORTABLE_FIELDS) + ")");
            }
        }
    }
}