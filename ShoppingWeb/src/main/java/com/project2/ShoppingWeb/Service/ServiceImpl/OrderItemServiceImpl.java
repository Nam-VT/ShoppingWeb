package com.project2.ShoppingWeb.Service.ServiceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project2.ShoppingWeb.DTO.OrderDTO;
import com.project2.ShoppingWeb.DTO.OrderItemDTO;
import com.project2.ShoppingWeb.Entity.Order;
import com.project2.ShoppingWeb.Entity.OrderItem;
import com.project2.ShoppingWeb.Entity.Product;
import com.project2.ShoppingWeb.Entity.User;
import com.project2.ShoppingWeb.Enums.OrderStatus;
import com.project2.ShoppingWeb.Exception.NotFoundException;
import com.project2.ShoppingWeb.Mapper.OrderMapper;
import com.project2.ShoppingWeb.Repository.OrderItemRepo;
import com.project2.ShoppingWeb.Repository.OrderRepo;
import com.project2.ShoppingWeb.Repository.ProductRepo;
import com.project2.ShoppingWeb.Service.OrderItemService;
import com.project2.ShoppingWeb.Service.UserService;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final ProductRepo productRepo;
    private final UserService userService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderDTO placeOrder(OrderDTO orderDTO) {
        User user = userService.getLoginUser();
        
        // Tạo đơn hàng mới
        Order order = new Order();
        order.setCustomerName(user.getName());
        order.setCustomerEmail(user.getEmail());
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setTotalPrice(orderDTO.getTotalPrice());
        
        // Xử lý các item
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
            Product product = productRepo.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found with id: " + itemDTO.getProductId()));
            
            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemDTO.getQuantity());
            item.setPrice(itemDTO.getPrice());
            item.setStatus(OrderStatus.PENDING);
            item.setUser(user);
            item.setOrder(order);
            
            orderItems.add(item);
        }
        
        order.setOrderItems(orderItems);
        
        // Lưu đơn hàng
        Order savedOrder = orderRepo.save(order);
        
        // Chuyển sang DTO và trả về
        return orderMapper.toDto(savedOrder);
    }

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
}
