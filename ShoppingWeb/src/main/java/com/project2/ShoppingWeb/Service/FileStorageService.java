package com.project2.ShoppingWeb.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.time.LocalDate;

@Service
public class FileStorageService {
    private Path rootLocation;
    private Path productLocation;
    
    @Value("${upload.path}")
    private String uploadPath;
    
    @Value("${upload.product-dir}")
    private String productDir;

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            // Khởi tạo paths sau khi properties được inject
            this.rootLocation = Paths.get(uploadPath);
            this.productLocation = rootLocation.resolve(productDir);
            
            // Tạo thư mục nếu chưa tồn tại
            Files.createDirectories(rootLocation);
            Files.createDirectories(productLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage locations", e);
        }
    }

    public String storeProductImage(MultipartFile file, String oldImagePath) {
        try {
            // Case 1: Không có file mới
            if (file == null || file.isEmpty()) {
                if (oldImagePath == null || oldImagePath.isEmpty()) {
                    return ""; // Trả về chuỗi rỗng thay vì null
                }
                return oldImagePath;  // Giữ nguyên ảnh cũ nếu là update
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Only image files are allowed");
            }

            // Validate file size (ví dụ: giới hạn 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new RuntimeException("File size exceeds the limit (5MB)");
            }

            // Generate unique filename
            String fileName = StringUtils.cleanPath(file.getOriginalFilename());
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String uniqueFileName = timeStamp + "_" + UUID.randomUUID().toString() + "_" + fileName;
            
            // Use configured path
            LocalDate today = LocalDate.now();
            Path dailyDir = productLocation.resolve(today.toString());
            
            // Create directory if not exists
            if (!Files.exists(dailyDir)) {
                Files.createDirectories(dailyDir);
            }

            // Delete old image if exists
            if (oldImagePath != null && !oldImagePath.isEmpty()) {
                try {
                    deleteProductImage(oldImagePath);
                } catch (Exception ex) {
                    // Log error but continue with saving new file
                    System.err.println("Warning: Failed to delete old image: " + ex.getMessage());
                }
            }

            // Save new file
            Path filePath = dailyDir.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path for database storage
            String relativePath = "/uploads/products/" + today + "/" + uniqueFileName;
            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("Could not store the file. IO Error: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw e; // Rethrow runtime exceptions
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error storing file: " + e.getMessage(), e);
        }
    }

    public void deleteProductImage(String imagePath) {
        // Skip if image path is null or empty
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }
        
        try {
            // Ensure path starts with / and remove it
            String normalizedPath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
            Path file = rootLocation.resolve(normalizedPath);
            
            if (Files.exists(file)) {
                Files.delete(file);
                System.out.println("Successfully deleted file: " + imagePath);
            } else {
                System.out.println("File not found for deletion: " + imagePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to delete file: " + imagePath + ", error: " + e.getMessage());
            // Don't throw exception to prevent disrupting the update process
        }
    }
}
