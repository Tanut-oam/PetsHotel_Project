package com.example.petshotel.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.petshotel.service.FileStorageService;

@Service
public class LocalFileStorageService implements FileStorageService {

    public static final String ROOM_IMAGE_URL_PREFIX = "/uploads/rooms/";

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);
    private static final long MAX_SIZE = 5L * 1024 * 1024;
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png");

    private final Path roomImageDir;

    public LocalFileStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.roomImageDir = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("rooms");
    }

    @Override
    public String storeRoomImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("กรุณาเลือกไฟล์รูป");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("ไฟล์รูปต้องมีขนาดไม่เกิน 5 MB");
        }
        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new IllegalArgumentException("รองรับเฉพาะไฟล์ JPG และ PNG");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new IllegalStateException("อ่านไฟล์รูปไม่สำเร็จ", e);
        }

        // ตรวจว่าเป็นรูปจริง ไม่ใช่ไฟล์อื่นที่ตั้งชนิดหลอกมา
        try {
            if (ImageIO.read(new ByteArrayInputStream(bytes)) == null) {
                throw new IllegalArgumentException("ไฟล์นี้ไม่ใช่รูปภาพที่ถูกต้อง");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("ไฟล์นี้ไม่ใช่รูปภาพที่ถูกต้อง");
        }

        // ตั้งชื่อใหม่เสมอ ไม่ใช้ชื่อไฟล์จากผู้ใช้
        String filename = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(roomImageDir);
            Files.write(roomImageDir.resolve(filename), bytes);
        } catch (IOException e) {
            throw new IllegalStateException("บันทึกไฟล์รูปไม่สำเร็จ", e);
        }
        return ROOM_IMAGE_URL_PREFIX + filename;
    }

    @Override
    public void deleteRoomImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith(ROOM_IMAGE_URL_PREFIX)) {
            return;
        }
        Path target = roomImageDir
                .resolve(imageUrl.substring(ROOM_IMAGE_URL_PREFIX.length()))
                .normalize();
        // กันการลบไฟล์นอกโฟลเดอร์รูปห้อง
        if (!target.getParent().equals(roomImageDir)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Could not delete room image {}", target, e);
        }
    }
}