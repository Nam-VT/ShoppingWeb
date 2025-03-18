package com.project2.ShoppingWeb.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.project2.ShoppingWeb.Entity.Product;
import com.project2.ShoppingWeb.Entity.Category;

public interface ProductRepo extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Category category);
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String nameKeyword, String descriptionKeyword);

}
