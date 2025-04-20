package com.project2.ShoppingWeb.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Sản phẩm được đánh giá

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người dùng đánh giá

    @Column(nullable = false)
    private String customerName; // Tên khách hàng

    @Column(nullable = false)
    private int rating; // Điểm đánh giá (1-5 sao)

    @Column(columnDefinition = "TEXT")
    private String comment; // Nội dung đánh giá

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // Ngày đánh giá

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now(); // Ngày cập nhật đánh giá
}

