package com.project2.ShoppingWeb.Service;

import com.project2.ShoppingWeb.DTO.OrderItemDTO;
import java.util.List;

public interface OrderItemService {
    OrderItemDTO updateOrderItemStatus(Long orderItemId, String status);
    List<OrderItemDTO> getOrderItemsByOrderId(Long orderId);
}
 


