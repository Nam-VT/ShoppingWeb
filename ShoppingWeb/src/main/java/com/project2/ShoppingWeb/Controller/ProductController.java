package com.project2.ShoppingWeb.Controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.project2.ShoppingWeb.Entity.Product;
import com.project2.ShoppingWeb.Exception.InvalidCredentialsException;
import com.project2.ShoppingWeb.Service.ProductService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.project2.ShoppingWeb.Exception.NotFoundException;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);

    @GetMapping("/get-all-product")
    public ResponseEntity<List<Product>> getAllProduct() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Product> createProduct(
        @RequestParam List<Long> categoryIds,
        @RequestParam MultipartFile image,
        @RequestParam String name,
        @RequestParam String description,
        @RequestParam BigDecimal price
    ) {
        if (categoryIds == null || name.isEmpty() || description.isEmpty() || price == null){
            throw new InvalidCredentialsException("All Fields are Required");
        }
        return ResponseEntity.ok(productService.createProduct(categoryIds, image, name, description, price));
    }
    
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateProduct(
        @RequestParam Long productId,
        @RequestParam Long categoryId,
        @RequestParam(required = false) MultipartFile image,
        @RequestParam String name,
        @RequestParam String description,
        @RequestParam String price
    ) {
        try {
            // Validate input
            if (productId == null || categoryId == null) {
                return ResponseEntity.badRequest().body("Product ID and Category ID are required");
            }
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Product name is required");
            }
            
            // Convert price from String to BigDecimal
            BigDecimal numericPrice;
            try {
                numericPrice = new BigDecimal(price);
                if (numericPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    return ResponseEntity.badRequest().body("Price must be greater than 0");
                }
            } catch (NumberFormatException e) {
                log.error("Invalid price format: {}", price);
                return ResponseEntity.badRequest().body("Invalid price format");
            }

            Product updatedProduct = productService.updateProduct(productId, categoryId, image, name, description, numericPrice);
            return ResponseEntity.ok(updatedProduct);
        } catch (NotFoundException e) {
            log.error("Not found error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error updating product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update product: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred");
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product Deleted Successfully");
    }

    @GetMapping("/get-product-by-id")
    public ResponseEntity<?> getProductById(@RequestParam Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (NotFoundException e) {
            log.error("Product not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving product: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to retrieve product: " + e.getMessage());
        }
    }

    @GetMapping("/get-product-by-category")
    public ResponseEntity<List<Product>> getProductByCategory(@RequestParam List<Long> categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategories(categoryId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProduct(@RequestParam String searchValue) {
        return ResponseEntity.ok(productService.searchProduct(searchValue));
    }

    /**
     * Lấy chi tiết sản phẩm kèm theo các đánh giá
     */
    @GetMapping("/details/{id}")
    public ResponseEntity<?> getProductWithReviews(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            log.error("Error retrieving product details: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to retrieve product details: " + e.getMessage());
        }
    }
}
