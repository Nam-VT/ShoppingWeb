package com.project2.ShoppingWeb.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.project2.ShoppingWeb.Entity.Review;

@Repository
public interface ReviewRepo extends JpaRepository<Review, Long> {

}
