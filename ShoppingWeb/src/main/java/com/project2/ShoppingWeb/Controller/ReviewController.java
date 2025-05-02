package com.project2.ShoppingWeb.Controller;

import com.project2.ShoppingWeb.Entity.Review;
import com.project2.ShoppingWeb.Entity.User;
import com.project2.ShoppingWeb.Service.ReviewService;
import com.project2.ShoppingWeb.Service.UserService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProductId(@PathVariable Long productId) {
        try {
            List<Review> reviews = reviewService.getReviewsByProductId(productId);
            return ResponseEntity.ok(reviews);
        } catch (RuntimeException e) {
            log.error("Error getting reviews for product ID {}: {}", productId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createReview(
            @RequestBody Map<String, Object> payload,
            Authentication authentication) {
        try {
            // Lấy thông tin từ payload
            Long productId = Long.parseLong(payload.get("productId").toString());
            String comment = (String) payload.get("comment");
            int rating = Integer.parseInt(payload.get("rating").toString());
            
            // Lấy thông tin người dùng đang đăng nhập
            User user = userService.getLoginUser();
            log.info("Creating review for user: {} with ID: {}", user.getEmail(), user.getId());
            
            // Tạo đối tượng review
            Review newReview = new Review();
            newReview.setComment(comment);
            newReview.setRating(rating);
            newReview.setCustomerName(user.getName());
            
            // Lưu review
            Review createdReview = reviewService.createReview(newReview, productId, Long.valueOf(user.getId()));
            return ResponseEntity.status(HttpStatus.CREATED).body(createdReview);
        } catch (Exception e) {
            log.error("Error creating review: {}", e.getMessage(), e);
            Map<String, String> errorResponse = Map.of("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateReview(
            @PathVariable Long id,
            @RequestBody Review review,
            Authentication authentication) {
        try {
            // Lấy thông tin người dùng đang đăng nhập
            User user = userService.getLoginUser();
            
            // Đặt ID cho review để đảm bảo cập nhật đúng bản ghi
            review.setId(id);
            Review updatedReview = reviewService.updateReview(review, Long.valueOf(user.getId()));
            return ResponseEntity.ok(updatedReview);
        } catch (RuntimeException e) {
            log.error("Error updating review: {}", e.getMessage());
            Map<String, String> errorResponse = Map.of("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        try {
            // Lấy thông tin người dùng đang đăng nhập
            User user = userService.getLoginUser();
            
            reviewService.deleteReview(id, Long.valueOf(user.getId()));
            return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Error deleting review: {}", e.getMessage());
            Map<String, String> errorResponse = Map.of("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}
