package com.project2.ShoppingWeb.Service.ServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project2.ShoppingWeb.Entity.Review;
import com.project2.ShoppingWeb.Entity.Product;
import com.project2.ShoppingWeb.Entity.User;
import com.project2.ShoppingWeb.Exception.NotFoundException;
import com.project2.ShoppingWeb.Repository.ReviewRepo;
import com.project2.ShoppingWeb.Service.ReviewService;      
import com.project2.ShoppingWeb.Repository.ProductRepo;
import com.project2.ShoppingWeb.Repository.UserRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepo reviewRepo;
    private final ProductRepo productRepo;
    private final UserRepo userRepo;

    @Override
    public List<Review> getAllReviews() {
        return reviewRepo.findAll();
    }

    @Override
    public Review getReviewById(Long id) {
        return reviewRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Review not found with ID: " + id));
    }

    @Override
    public List<Review> getReviewsByProductId(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return reviewRepo.findByProduct(product);
    }
    
    @Override
    @Transactional
    public Review createReview(Review review, Long productId, int userId) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Kiểm tra xem người dùng đã đánh giá sản phẩm này chưa
        if (reviewRepo.existsByProductAndUser(product, user)) {
            throw new RuntimeException("You have already reviewed this product");
        }
        
        review.setProduct(product);
        review.setUser(user);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        
        return reviewRepo.save(review);
    }
    
    @Override
    @Transactional
    public Review updateReview(Review review, int userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Kiểm tra nếu review tồn tại và thuộc về user
        Review existingReview = reviewRepo.findById(review.getId())
            .orElseThrow(() -> new RuntimeException("Review not found"));
            
        if (existingReview.getUser().getId() != userId) {
            throw new RuntimeException("You can only update your own reviews");
        }
        
        // Cập nhật thông tin
        existingReview.setComment(review.getComment());
        existingReview.setRating(review.getRating());
        existingReview.setUpdatedAt(LocalDateTime.now());
        
        return reviewRepo.save(existingReview);
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId, int userId) {
        // Kiểm tra nếu review tồn tại và thuộc về user
        Review review = reviewRepo.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
            
        if (review.getUser().getId() != userId) {
            throw new RuntimeException("You can only delete your own reviews");
        }
        
        reviewRepo.delete(review);
    }
}
