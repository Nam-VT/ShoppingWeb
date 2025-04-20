package com.project2.ShoppingWeb.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project2.ShoppingWeb.Entity.Review;
import com.project2.ShoppingWeb.Entity.Product;
import com.project2.ShoppingWeb.Entity.User;

@Repository
public interface ReviewRepo extends JpaRepository<Review, Long> {

    List<Review> findByProduct(Product product);
    
    boolean existsByProductAndUser(Product product, User user);
}
