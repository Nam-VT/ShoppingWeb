package com.project2.ShoppingWeb.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Long id;
    private String status;
    private Long orderId;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private int quantity;
    private BigDecimal price;
    private Integer userId;
    private String userName;
    private LocalDateTime createdAt;
}
