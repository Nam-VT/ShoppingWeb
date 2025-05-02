package com.project2.ShoppingWeb.Entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;


import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String name; // Tên sản phẩm

    @Column(length = 500)
    private String description; // Mô tả sản phẩm

    @Column(nullable = false)
    private BigDecimal price; // Giá sản phẩm

    @Column(nullable = false)
    private int stockQuantity; // Số lượng tồn kho

    @Column(nullable = false)
    private int soldQuantity; // Số lượng đã bán

    @JsonManagedReference
    @ManyToMany
    @JoinTable(
            name = "product_category",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories; // Danh mục sản phẩm

    @Column(name = "image_url")
    private String imageUrl; // Đường dẫn ảnh sản phẩm

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    @Builder.Default
    private boolean isActive = true; // Trạng thái sản phẩm

    @JsonManagedReference
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews; // Danh sách đánh giá sản phẩm

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(); // Ngày tạo

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now(); // Ngày cập nhật

    @JsonManagedReference(value = "product-items")
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;

    @Column(columnDefinition = "TEXT")
    private String embeddingJson; // Vector embedding cho semantic search

}


