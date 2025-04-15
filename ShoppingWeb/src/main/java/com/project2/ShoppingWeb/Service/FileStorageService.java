package com.project2.ShoppingWeb.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

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

    public String storeProductImage(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed to store empty file");
            }

            // Tạo tên file unique với timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String originalFilename = file.getOriginalFilename();
            if(originalFilename == null){
                throw new RuntimeException("Failed to store empty file");
            }
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = timestamp + "_" + UUID.randomUUID().toString() + extension;

            // Tạo thư mục theo ngày
            String dateFolder = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            Path datePath = productLocation.resolve(dateFolder);
            Files.createDirectories(datePath);

            // Lưu file
            Path destinationFile = datePath.resolve(filename);
            Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

            // Trả về đường dẫn relative để lưu vào database
            return String.format("/uploads/products/%s/%s", dateFolder, filename);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public void deleteProductImage(String imagePath) {
        try {
            Path file = rootLocation.resolve(imagePath.substring(1)); // Bỏ dấu / ở đầu
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}
