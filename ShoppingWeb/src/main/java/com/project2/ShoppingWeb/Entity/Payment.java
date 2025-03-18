package com.project2.ShoppingWeb.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.project2.ShoppingWeb.Enums.PaymentMethod;
import com.project2.ShoppingWeb.Enums.PaymentStatus;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order; // Đơn hàng tương ứng

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method; // Phương thức thanh toán

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING; // Trạng thái thanh toán

    @Column(nullable = false)
    private BigDecimal amount; // Số tiền thanh toán

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // Ngày tạo giao dịch

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now(); // Ngày cập nhật trạng thái
}
