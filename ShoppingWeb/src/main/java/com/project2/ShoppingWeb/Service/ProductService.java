package com.project2.ShoppingWeb.Service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.project2.ShoppingWeb.Entity.Product;


public interface ProductService {
    Product createProduct(List<Long> categoryIds, MultipartFile image, String name, String description, BigDecimal price);
    // Product updateProduct(Long productId, Long categoryId, MultipartFile image, String name, String description, BigDecimal price);
    // Product createProduct(List<Long> categoryIds, String name, String description, BigDecimal price);
    Product updateProduct(Long productId, List<Long> categoryId, String name, String description, BigDecimal price);
    void deleteProduct(Long productId);
    Product getProductById(Long productId);
    List<Product> getAllProducts();
    List<Product> getProductsByCategories(List<Long> categoryIds);
    List<Product> searchProduct(String searchValue);

} 

