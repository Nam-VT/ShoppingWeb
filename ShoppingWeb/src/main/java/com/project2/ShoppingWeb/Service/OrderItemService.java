package com.project2.ShoppingWeb.Service;

import com.project2.ShoppingWeb.DTO.OrderDTO;
import com.project2.ShoppingWeb.DTO.OrderItemDTO;
import com.project2.ShoppingWeb.Entity.OrderItem;

public interface OrderItemService {
    OrderDTO placeOrder(OrderDTO orderDTO);
    OrderItemDTO updateOrderItemStatus(Long orderItemId, String status);
}
 


