package com.project2.ShoppingWeb.Entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName; // Tên khách hàng

    @Column(nullable = false)
    private String customerEmail; // Email khách hàng

    @Column(nullable = false)
    private String shippingAddress; // Địa chỉ giao hàng

    @Column(nullable = false)
    private BigDecimal totalPrice; // Tổng tiền đơn hàng

    @JsonManagedReference(value = "order-items")
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems; // Danh sách sản phẩm trong đơn hàng

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(); // Ngày tạo đơn

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now(); // Ngày cập nhật đơn
}
