package com.project2.ShoppingWeb.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import com.project2.ShoppingWeb.Service.ProductService;
import com.project2.ShoppingWeb.Entity.Product;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project2.ShoppingWeb.Exception.InvalidCredentialsException;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/get-all-product")
    public ResponseEntity<List<Product>> getAllProduct() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Product> createProduct(
        @RequestParam List<Long> categoryId,
//        @RequestParam MultipartFile image,
        @RequestParam String name,
        @RequestParam String description,
        @RequestParam BigDecimal price
    ) {
        if (categoryId == null || name.isEmpty() || description.isEmpty() || price == null){
            throw new InvalidCredentialsException("All Fields are Required");
        }
        return ResponseEntity.ok(productService.createProduct(categoryId, name, description, price));
    }
    
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Product> updateProduct(
        @RequestParam Long productId,
        @RequestParam List<Long> categoryId,
        @RequestParam String name,
        @RequestParam String description,
        @RequestParam BigDecimal price
    ) {
        if (productId == null || categoryId == null || name.isEmpty() || description.isEmpty() || price == null){
            throw new InvalidCredentialsException("All Fields are Required");
        }
        return ResponseEntity.ok(productService.updateProduct(productId, categoryId, name, description, price));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product Deleted Successfully");
    }

    @GetMapping("/get-product-by-id")
    public ResponseEntity<Product> getProductById(@RequestParam Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/get-product-by-category")
    public ResponseEntity<List<Product>> getProductByCategory(@RequestParam List<Long> categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategories(categoryId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProduct(@RequestParam String searchValue) {
        return ResponseEntity.ok(productService.searchProduct(searchValue));
    }

    
}
