package com.project2.ShoppingWeb.Service.ServiceImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.project2.ShoppingWeb.Entity.Category;
import com.project2.ShoppingWeb.Entity.Product;
import com.project2.ShoppingWeb.Exception.NotFoundException;
import com.project2.ShoppingWeb.Repository.CategoryRepo;
import com.project2.ShoppingWeb.Repository.ProductRepo;
import com.project2.ShoppingWeb.Service.FileStorageService;
import com.project2.ShoppingWeb.Service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;
    private final FileStorageService fileStorageService;
    
    // private LocalImageService localImageService;

    // @Override
    // public Product createProduct(List<Long> categoryIds, MultipartFile image, String name, String description, BigDecimal price) {
    //     // TODO Auto-generated method stub
    //     List<Category> categories = categoryRepository.findAllById(categoryIds);
    //     String imagePath = localImageService.saveImage(image);
        
    //     Product product = new Product();
    //     product.setCategory(category);
    //     product.setName(name);
    //     product.setDescription(description);
    //     product.setPrice(price);
    //     product.setImage(imagePath);

    //     productRepo.save(product);
    //     return product;  
        
    // }

    // @Override   
    // public Product updateProduct(Long id, List<Long> categoryIds, MultipartFile image, String name, String description, BigDecimal price) {
    //     // TODO Auto-generated method stub
    //     Product product = productRepo.findById(id).orElse(null);
    //     if (product == null) {
    //         throw new NotFoundException("Product with ID " + id + " not found");
    //     }

    //     List<Category> categories = categoryRepository.findAllById(categoryIds);
    //     String imagePath = localImageService.saveImage(image);
        
    //     product.setCategory(category);
    //     product.setName(name);
    //     product.setDescription(description);
    //     product.setPrice(price);
    //     product.setImage(imagePath);

    //     productRepo.save(product);
    //     return product;
    // }

    @Override
    public Product createProduct(List<Long> categoryIds,  MultipartFile image, String name, String description, BigDecimal price) {
        try {
            log.info("Creating product with name: {}", name);
            
            // Validate input
            if (categoryIds == null || categoryIds.isEmpty()) {
                throw new NotFoundException("At least one category is required");
            }
            
            // Store image
            String imageUrl = fileStorageService.storeProductImage(image, null);
            
            // Find categories
            List<Category> categories = categoryRepo.findAllById(categoryIds);
            if(categories.isEmpty()) {
                throw new NotFoundException("No categories found with the provided IDs");
            }
            
            // Create product with default values
            Product product = Product.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .imageUrl(imageUrl)
                    .categories(categories)
                    .stockQuantity(0) // Default value
                    .soldQuantity(0)  // Default value
                    .isActive(true)   // Default value
                    .build();
            
            Product savedProduct = productRepo.save(product);
            log.info("Product created successfully with ID: {}", savedProduct.getId());
            return savedProduct;
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create product: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Product updateProduct(Long productId, Long categoryId, MultipartFile image, 
        String name, String description, BigDecimal price) {
        
        try {
            log.info("Updating product ID: {}", productId);
            
            // Find product
            Product product = productRepo.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + productId));
            
            // Find category
            Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with ID: " + categoryId));
            
            // Update basic info
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setUpdatedAt(LocalDateTime.now());
            
            // Update image if provided
            if (image != null && !image.isEmpty()) {
                String currentImageUrl = product.getImageUrl();
                try {
                    String newImageUrl = fileStorageService.storeProductImage(image, currentImageUrl);
                    product.setImageUrl(newImageUrl);
                } catch (Exception e) {
                    log.error("Error updating product image: {}", e.getMessage());
                    throw new RuntimeException("Failed to update product image", e);
                }
            }
            
            // Update category - use ArrayList instead of Collections.singletonList
            List<Category> categories = new ArrayList<>();
            categories.add(category);
            product.setCategories(categories);
            
            Product updatedProduct = productRepo.save(product);
            log.info("Product updated successfully: {}", updatedProduct.getId());
            return updatedProduct;
        } catch (NotFoundException e) {
            log.error("Not found error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error updating product: {}", e.getMessage());
            throw new RuntimeException("Failed to update product: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Xóa ảnh trước
        if (product.getImageUrl() != null) {
            fileStorageService.deleteProductImage(product.getImageUrl());
        }

        // Xóa product
        productRepo.delete(product);
    }

    @Override
    public Product getProductById(Long id) {
        return productRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product with ID " + id + " not found"));
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

    @Override
    public List<Product> getProductsByCategories(List<Long> categoryIds) {
        List<Category> categories = categoryRepo.findAllById(categoryIds);
        if(categories.isEmpty()) {
            throw new NotFoundException("Category not found");
        }

        return productRepo.findByCategories(categories);
    }

    @Override
    public List<Product> searchProduct(String searchValue) {
        return productRepo.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(searchValue, searchValue);
    }
}   
