package com.marketplace.controller;

import com.marketplace.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    // Upload single image
    @PostMapping("/image")
    public ResponseEntity<Map<String, Object>> uploadImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = fileUploadService.uploadImage(file);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Image uploaded successfully");
        response.put("imageUrl", imageUrl);
        response.put("fileName", file.getOriginalFilename());

        return ResponseEntity.ok(response);
    }

    // Upload multiple images
    @PostMapping("/images")
    public ResponseEntity<Map<String, Object>> uploadMultipleImages(@RequestParam("files") MultipartFile[] files) {
        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            String imageUrl = fileUploadService.uploadImage(file);
            imageUrls.add(imageUrl);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Images uploaded successfully");
        response.put("imageUrls", imageUrls);
        response.put("count", imageUrls.size());

        return ResponseEntity.ok(response);
    }

    // Upload avatar
    @PostMapping("/avatar")
    public ResponseEntity<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = fileUploadService.uploadAvatar(file);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Avatar uploaded successfully");
        response.put("avatarUrl", avatarUrl);

        return ResponseEntity.ok(response);
    }

    // Delete image
    @DeleteMapping("/image")
    public ResponseEntity<Map<String, Object>> deleteImage(@RequestParam("imageUrl") String imageUrl) {
        fileUploadService.deleteImage(imageUrl);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Image deleted successfully");

        return ResponseEntity.ok(response);
    }

    // Get upload limits info
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getUploadInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("maxFileSize", "10MB");
        info.put("allowedTypes", List.of("image/jpeg", "image/jpg", "image/png", "image/gif"));
        info.put("maxFilesPerUpload", 5);

        return ResponseEntity.ok(info);
    }
}