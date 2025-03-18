package com.project2.ShoppingWeb.Service.ServiceImpl;

import org.springframework.stereotype.Service;
import com.project2.ShoppingWeb.Repository.CategoryRepo;
import com.project2.ShoppingWeb.Entity.Category;
import com.project2.ShoppingWeb.Service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private CategoryRepo categoryRepo;

    @Override
    public Category createCategory(Category category) {
        // TODO Auto-generated method stub
        return categoryRepo.save(category);
    }

    @Override
    public Category updateCategory(Long categoryId, String name) {
        // TODO Auto-generated method stub
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(name);
        Category updatedCategory = categoryRepo.save(category);
        return updatedCategory;
    }

    @Override
    public void deleteCategory(Long categoryId) {
        // TODO Auto-generated method stub
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        categoryRepo.delete(category);
    }

    @Override
    public Category getCategoryById(Long categoryId) {
        // TODO Auto-generated method stub
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return category;
    }

    @Override
    public List<Category> getAllCategories() {
        // TODO Auto-generated method stub
        return categoryRepo.findAll();
    }

    @Override
    public List<Category> getCategoriesByProduct(Long productId) {
        // TODO Auto-generated method stub
        return categoryRepo.findByProductsId(productId);
    }

    @Override
    public List<Category> searchCategory(String searchValue) {
        // TODO Auto-generated method stub
        return categoryRepo.findByNameContainingIgnoreCase(searchValue);
    }

}