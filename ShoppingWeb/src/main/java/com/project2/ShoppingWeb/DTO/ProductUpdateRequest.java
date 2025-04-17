package com.project2.ShoppingWeb.DTO;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {
    private Long productId;
    private Long categoryId;
    private String name;
    private String description;
    private BigDecimal price;
}

