package com.project2.ShoppingWeb.Service;

import com.project2.ShoppingWeb.Entity.OrderItem;
import com.project2.ShoppingWeb.Entity.Order;
import com.project2.ShoppingWeb.DTO.OrderRequest;
import com.project2.ShoppingWeb.Enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemService {
    Order placeOrder(OrderRequest orderRequest);
    Order updateOrderItemStatus(Long orderItemId, String status);
    List<OrderItem> filterOrderItems(OrderStatus status, LocalDateTime startDate,
                                        Long itemId);
    

}
