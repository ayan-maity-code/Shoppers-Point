package com.shopperspoint.utill;


import com.shopperspoint.exceptionhandler.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
@Slf4j
public class ImageUtils {

    public static void uploadImage(MultipartFile image, Long id, String type) {
        if (image != null && !image.isEmpty()) {
            String originalFilename = image.getOriginalFilename();
            String extension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            List<String> allowedExtensions = List.of(".jpg", ".jpeg", ".png", ".bmp", ".webp");
            if (!allowedExtensions.contains(extension.toLowerCase())) {
                log.error("Invalid file type: {}. Only JPG, JPEG, PNG, and WEBP are allowed.", extension);
                throw new BadRequestException("Invalid file type. Only JPG, JPEG, PNG, and WEBP are allowed.");
            }

            String uploadFolder = "images/" + type;
            Path uploadPath = Paths.get(uploadFolder).toAbsolutePath();
            try {
                Files.createDirectories(uploadPath);
                log.info("Upload directory created at: {}", uploadPath);
            } catch (IOException e) {
                log.error("Failed to create upload directory: {}", e.getMessage());
                throw new BadRequestException("Failed to create upload directory");
            }
            Path imagePath = uploadPath.resolve(id + extension);
            try {
                image.transferTo(imagePath.toFile());
                log.info("Image successfully uploaded to: {}", imagePath);
            } catch (IOException e) {
                log.error("Failed to save image: {}", e.getMessage());
                throw new BadRequestException("Failed to save image");

            }
        } else {
            log.warn("No image uploaded for ID: {}", id);
        }
    }


    public static String getImage(Long userId, String type) {
        List<String> exts = List.of(".jpg", ".jpeg", ".png", ".webp");
        String basePath = System.getProperty("user.dir") + "/images/" + type + "/";

        for (String ext : exts) {
            File file = new File(basePath + userId + ext);
            if (file.exists()) {
                return "http://localhost:8080/images/" + type + "/" + userId + ext;
            }
        }
        log.warn("No image found for ID: {}", userId);
        return null;
    }

}
