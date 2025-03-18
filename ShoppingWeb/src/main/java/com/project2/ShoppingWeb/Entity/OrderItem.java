package com.project2.ShoppingWeb.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import com.project2.ShoppingWeb.Enums.OrderStatus;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING; // Trạng thái đơn hàng

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // Đơn hàng chứa sản phẩm

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Sản phẩm thuộc đơn hàng

    @Column(nullable = false)
    private int quantity; // Số lượng sản phẩm

    @Column(nullable = false)
    private BigDecimal price; // Giá sản phẩm tại thời điểm mua

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Người mua

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}

