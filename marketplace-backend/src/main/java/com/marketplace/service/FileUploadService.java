package com.marketplace.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.marketplace.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileUploadService {

    @Autowired
    private Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public String uploadImage(MultipartFile file) {
        validateImageFile(file);
        return uploadToCloudinary(file, "marketplace/products");
    }

    public String uploadAvatar(MultipartFile file) {
        validateImageFile(file);
        return uploadToCloudinary(file, "marketplace/avatars");
    }

    public void deleteImage(String imageUrl) {
        try {
            // Extract public_id from Cloudinary URL
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            throw new BadRequestException("Không thể xóa file: " + e.getMessage());
        }
    }

    private String uploadToCloudinary(MultipartFile file, String folder) {
        try {
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image",
                "transformation", new Transformation()
                    .width(800).height(800).crop("limit").quality("auto")
            );

            Map<String, Object> result = cloudinary.uploader().upload(
                file.getBytes(), 
                uploadParams
            );

            return (String) result.get("secure_url");

        } catch (IOException e) {
            throw new BadRequestException("Không thể upload file: " + e.getMessage());
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length > 1) {
                String afterUpload = parts[1];
                if (afterUpload.contains("/v")) {
                    afterUpload = afterUpload.substring(afterUpload.indexOf("/", 2) + 1);
                }
                int lastDotIndex = afterUpload.lastIndexOf(".");
                if (lastDotIndex > 0) {
                    afterUpload = afterUpload.substring(0, lastDotIndex);
                }
                return afterUpload;
            }
        } catch (Exception e) {
        }
        return null;
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