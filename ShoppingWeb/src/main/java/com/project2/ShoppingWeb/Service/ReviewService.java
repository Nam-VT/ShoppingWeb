package com.project2.ShoppingWeb.Service;

import java.util.List;

import com.project2.ShoppingWeb.Entity.Review;

public interface ReviewService {
    
    /**
     * Lấy tất cả đánh giá
     */
    List<Review> getAllReviews();
    
    /**
     * Lấy đánh giá theo ID
     */
    Review getReviewById(Long id);
    
    /**
     * Lấy tất cả đánh giá cho một sản phẩm
     */
    List<Review> getReviewsByProductId(Long productId);
    
    /**
     * Tạo đánh giá mới
     */
    Review createReview(Review review, Long productId, Long userId);
    
    /**
     * Cập nhật đánh giá
     */
    Review updateReview(Review review, Long userId);
    
    /**
     * Xóa đánh giá
     */
    void deleteReview(Long reviewId, Long userId);
}
