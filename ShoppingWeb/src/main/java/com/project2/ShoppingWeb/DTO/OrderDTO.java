package com.project2.ShoppingWeb.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.project2.ShoppingWeb.Enums.OrderStatus;
import com.project2.ShoppingWeb.Enums.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String customerName;
    private String customerEmail;
    private String shippingAddress;
    private BigDecimal totalPrice;
    private List<OrderItemDTO> orderItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String paymentMethod;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
}
