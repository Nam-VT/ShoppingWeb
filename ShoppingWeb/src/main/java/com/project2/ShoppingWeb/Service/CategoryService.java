package com.project2.ShoppingWeb.Service;

import com.project2.ShoppingWeb.Entity.Category;
import java.util.List;

public interface CategoryService {
    Category createCategory(Category category);
    Category updateCategory(Long categoryId, String name);
    void deleteCategory(Long categoryId);
    Category getCategoryById(Long categoryId);
    List<Category> getAllCategories();
    List<Category> getCategoriesByProduct(Long productId);
    List<Category> searchCategory(String searchValue);

}
