package com.project2.ShoppingWeb.Service;

import com.project2.ShoppingWeb.Entity.Product;

import java.util.List;
import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;


public interface ProductService {
    // Product createProduct(List<Long> categoryIds, MultipartFile image, String name, String description, BigDecimal price);
    // Product updateProduct(Long productId, Long categoryId, MultipartFile image, String name, String description, BigDecimal price);
    Product createProduct(List<Long> categoryIds, String name, String description, BigDecimal price);
    Product updateProduct(Long productId, List<Long> categoryId, String name, String description, BigDecimal price);
    void deleteProduct(Long productId);
    Product getProductById(Long productId);
    List<Product> getAllProducts();
    List<Product> getProductsByCategory(Long categoryId);
    List<Product> searchProduct(String searchValue);
} 

