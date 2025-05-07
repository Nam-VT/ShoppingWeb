package com.project2.ShoppingWeb.Service;

import com.project2.ShoppingWeb.DTO.OrderDTO;
import com.project2.ShoppingWeb.DTO.OrderItemDTO;
import java.util.List;

public interface OrderService {
    OrderDTO placeOrder(OrderDTO orderDTO);
    OrderItemDTO updateOrderItemStatus(Long orderItemId, String status);
    OrderDTO getOrderById(Long orderId);
    List<OrderDTO> getCurrentUserOrders();
    OrderDTO updateOrderStatus(Long orderId, String status);
    OrderDTO updatePaymentStatus(Long orderId, String status);

    List<OrderDTO> getAllOrders();
    List<OrderDTO> getOrdersByStatus(String status);
    List<OrderDTO> getOrdersByUserId(Long userId);
    void deleteOrder(Long orderId);
}
 


