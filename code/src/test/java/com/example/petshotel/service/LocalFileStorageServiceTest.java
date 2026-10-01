package com.example.petshotel.service;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.example.petshotel.service.impl.LocalFileStorageService;

class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageService storage;

    @BeforeEach
    void setUp() {
        storage = new LocalFileStorageService(tempDir.toString());
    }

    private byte[] pngBytes() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", out);
        return out.toByteArray();
    }

    @Test
    void storeRoomImage_savesValidPng() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "room.png", "image/png", pngBytes());

        String url = storage.storeRoomImage(file);

        assertTrue(url.startsWith("/uploads/rooms/"));
        assertTrue(url.endsWith(".png"));
        String filename = url.substring("/uploads/rooms/".length());
        assertTrue(Files.exists(tempDir.resolve("rooms").resolve(filename)));
    }

    @Test
    void storeRoomImage_rejectsFakeImage() {
        MockMultipartFile file = new MockMultipartFile("image", "evil.png", "image/png",
                "<script>alert(1)</script>".getBytes());

        assertThrows(IllegalArgumentException.class, () -> storage.storeRoomImage(file));
    }

    @Test
    void storeRoomImage_rejectsUnsupportedType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "room.gif", "image/gif", pngBytes());

        assertThrows(IllegalArgumentException.class, () -> storage.storeRoomImage(file));
    }

    @Test
    void storeRoomImage_rejectsTooLargeFile() {
        MockMultipartFile file = new MockMultipartFile("image", "big.png", "image/png",
                new byte[5 * 1024 * 1024 + 1]);

        assertThrows(IllegalArgumentException.class, () -> storage.storeRoomImage(file));
    }

    @Test
    void storeRoomImage_rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("image", "empty.png", "image/png", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> storage.storeRoomImage(file));
    }

    @Test
    void deleteRoomImage_removesStoredFile() throws Exception {
        String url = storage.storeRoomImage(
                new MockMultipartFile("image", "room.png", "image/png", pngBytes()));
        Path stored = tempDir.resolve("rooms").resolve(url.substring("/uploads/rooms/".length()));

        storage.deleteRoomImage(url);

        assertFalse(Files.exists(stored));
    }

    @Test
    void deleteRoomImage_ignoresPathTraversal() throws Exception {
        Path outside = Files.writeString(tempDir.resolve("secret.txt"), "keep me");

        storage.deleteRoomImage("/uploads/rooms/../secret.txt");

        assertTrue(Files.exists(outside));
    }
}