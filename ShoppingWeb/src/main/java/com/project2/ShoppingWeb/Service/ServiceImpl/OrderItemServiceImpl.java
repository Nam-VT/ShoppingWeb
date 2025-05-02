package com.project2.ShoppingWeb.Service.ServiceImpl;

import org.springframework.stereotype.Service;

import com.project2.ShoppingWeb.DTO.OrderItemDTO;
import com.project2.ShoppingWeb.Entity.OrderItem;
import com.project2.ShoppingWeb.Enums.OrderStatus;
import com.project2.ShoppingWeb.Exception.NotFoundException;
import com.project2.ShoppingWeb.Mapper.OrderMapper;
import com.project2.ShoppingWeb.Repository.OrderItemRepo;
import com.project2.ShoppingWeb.Service.OrderItemService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepo orderItemRepo;
    private final OrderMapper orderMapper;

    @Override
    public OrderItemDTO updateOrderItemStatus(Long orderItemId, String status) {
        OrderItem orderItem = orderItemRepo.findById(orderItemId)
                .orElseThrow(() -> new NotFoundException("Order Item not found"));

        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            orderItem.setStatus(orderStatus);
            OrderItem updatedItem = orderItemRepo.save(orderItem);
            return orderMapper.toDto(updatedItem);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }

    @Override
    public List<OrderItemDTO> getOrderItemsByOrderId(Long orderId) {
        // Implementation needed
        throw new UnsupportedOperationException("Method not implemented");
    }
}
