package com.project2.ShoppingWeb.Repository;

import com.project2.ShoppingWeb.Entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepo extends JpaRepository<Category, Long> {
    List<Category> findByNameContainingIgnoreCase(String value);
    List<Category> findByProductsId(Long id);
}
