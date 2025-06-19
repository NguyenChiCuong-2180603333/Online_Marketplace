package com.marketplace.service;

import com.marketplace.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final String UPLOAD_DIR = "uploads/";
    private static final String IMAGE_DIR = UPLOAD_DIR + "images/";
    private static final String AVATAR_DIR = UPLOAD_DIR + "avatars/";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public String uploadImage(MultipartFile file) {
        validateImageFile(file);
        return saveFile(file, IMAGE_DIR);
    }

    public String uploadAvatar(MultipartFile file) {
        validateImageFile(file);
        return saveFile(file, AVATAR_DIR);
    }

    public void deleteImage(String imageUrl) {
        try {
            // Extract filename from URL
            String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new BadRequestException("Không thể xóa file: " + e.getMessage());
        }
    }

    private String saveFile(MultipartFile file, String directory) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(directory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Save file
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return URL - in production, this would be your CDN/storage URL
            return "/api/files/" + directory.replace(UPLOAD_DIR, "") + uniqueFileName;

        } catch (IOException e) {
            throw new BadRequestException("Không thể lưu file: " + e.getMessage());
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File không được để trống");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Kích thước file không được vượt quá 10MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new BadRequestException("Chỉ cho phép upload file ảnh (JPG, PNG, GIF)");
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif");
    }
}