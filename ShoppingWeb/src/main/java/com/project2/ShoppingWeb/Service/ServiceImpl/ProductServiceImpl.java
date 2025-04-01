package com.project2.ShoppingWeb.Service.ServiceImpl;

import com.project2.ShoppingWeb.Service.ProductService;
import com.project2.ShoppingWeb.Exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.project2.ShoppingWeb.Repository.CategoryRepo;
import com.project2.ShoppingWeb.Repository.ProductRepo;

import com.project2.ShoppingWeb.Entity.Category;
import com.project2.ShoppingWeb.Entity.Product;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepo productRepo;
    private final CategoryRepo categoryRepo;

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
    public Product createProduct(List<Long> categoryIds, String name, String description, BigDecimal price) {
        // TODO Auto-generated method stub
        List<Category> categories = categoryRepo.findAllById(categoryIds);
        if(categories.isEmpty()) {
            throw new NotFoundException("Category not found");
        }
        Product product = new Product();
        product.setCategories(categories);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);

        productRepo.save(product);
        return product;  
        
    }

    @Override   
    public Product updateProduct(Long id, List<Long> categoryIds, String name, String description, BigDecimal price) {
        // TODO Auto-generated method stub
        Product product = productRepo.findById(id).orElse(null);
        if (product == null) {
            throw new NotFoundException("Product with ID " + id + " not found");
        }
        List<Category> categories = categoryRepo.findAllById(categoryIds);
        if(categories.isEmpty()) {
            throw new NotFoundException("Category not found");
        }
        
        product.setCategories(categories);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);

        productRepo.save(product);
        return product;
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Product with ID " + id + " not found"));

        productRepo.delete(product);
        log.info("Product with ID {} has been deleted successfully.", id);
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
