package com.example.petshotel.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String storeRoomImage(MultipartFile file);
    void deleteRoomImage(String imageUrl);
    String storePetImage(MultipartFile file);
    void deletePetImage(String imageUrl);
}