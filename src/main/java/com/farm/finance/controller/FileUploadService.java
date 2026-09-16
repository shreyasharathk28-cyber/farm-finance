package com.farm.finance.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final String UPLOAD_DIR = "uploads/";

    public String uploadPhoto(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.write(filePath, file.getBytes());

            System.out.println("✅ Photo uploaded: " + fileName);
            return "/uploads/" + fileName;

        } catch (IOException e) {
            System.out.println("❌ Failed to upload photo: " + e.getMessage());
            return null;
        }
    }
}