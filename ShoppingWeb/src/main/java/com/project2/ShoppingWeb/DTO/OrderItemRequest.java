package com.project2.ShoppingWeb.DTO;


import lombok.Data;

@Data
public class OrderItemRequest {

    private Long productId;
    private int quantity;
}
