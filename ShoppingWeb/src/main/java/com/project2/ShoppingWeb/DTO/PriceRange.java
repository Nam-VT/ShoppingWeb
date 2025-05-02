package com.project2.ShoppingWeb.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PriceRange {
    private Long min;  // giá tối thiểu
    private Long max;  // giá tối đa
}