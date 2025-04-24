package com.project2.ShoppingWeb.Mapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.project2.ShoppingWeb.DTO.OrderDTO;
import com.project2.ShoppingWeb.DTO.OrderItemDTO;
import com.project2.ShoppingWeb.Entity.Order;
import com.project2.ShoppingWeb.Entity.OrderItem;
import com.project2.ShoppingWeb.Entity.Product;
import com.project2.ShoppingWeb.Entity.User;
import com.project2.ShoppingWeb.Enums.OrderStatus;
import com.project2.ShoppingWeb.Enums.PaymentStatus;
import com.project2.ShoppingWeb.Repository.OrderRepo;
import com.project2.ShoppingWeb.Repository.ProductRepo;
import com.project2.ShoppingWeb.Repository.UserRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderMapper {
    
    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final OrderRepo orderRepo;
    
    // Entity -> DTO
    public OrderDTO toDto(Order entity) {
        if (entity == null) return null;
        
        List<OrderItemDTO> orderItemsDto = entity.getOrderItems() == null ? 
            new ArrayList<>() : 
            entity.getOrderItems().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        
        return OrderDTO.builder()
                .id(entity.getId())
                .customerName(entity.getCustomerName())
                .customerEmail(entity.getCustomerEmail())
                .shippingAddress(entity.getShippingAddress())
                .totalPrice(entity.getTotalPrice())
                .orderItems(orderItemsDto)
                .status(entity.getStatus())
                .paymentStatus(entity.getPaymentStatus())
                .paymentMethod(entity.getPaymentMethod())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    // DTO -> Entity
    public Order toEntity(OrderDTO dto) {
        if (dto == null) return null;
        
        Order order = new Order();
        // Không set ID khi tạo mới, chỉ khi cập nhật
        if (dto.getId() != null) {
            order.setId(dto.getId());
        }
        
        order.setCustomerName(dto.getCustomerName());
        order.setCustomerEmail(dto.getCustomerEmail());
        order.setShippingAddress(dto.getShippingAddress());
        order.setTotalPrice(dto.getTotalPrice());
        order.setPaymentMethod(dto.getPaymentMethod());
        
        order.setStatus(dto.getStatus() != null ? dto.getStatus() : OrderStatus.PENDING);
        
        order.setPaymentStatus(dto.getPaymentStatus() != null ? dto.getPaymentStatus() : PaymentStatus.PENDING);
        
        return order;
    }
    
    // Chuyển đổi OrderItemDTO -> OrderItem Entity
    public OrderItem toEntity(OrderItemDTO dto, Order order) {
        if (dto == null) return null;
        
        OrderItem item = new OrderItem();
        if (dto.getId() != null) {
            item.setId(dto.getId());
        }
        
        // Lấy product từ repository
        Product product = null;
        if (dto.getProductId() != null) {
            product = productRepo.findById(dto.getProductId())
                .orElse(null);
            if (product == null) {
                log.warn("Product with id {} not found", dto.getProductId());
            }
        }
        
        item.setProduct(product);
        item.setQuantity(dto.getQuantity());
        item.setPrice(dto.getPrice());
        
        // Chuyển đổi String thành OrderStatus
        if (dto.getStatus() != null) {
            try {
                item.setStatus(OrderStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status value: {}. Setting to PENDING.", dto.getStatus());
                item.setStatus(OrderStatus.PENDING);
            }
        } else {
            item.setStatus(OrderStatus.PENDING);
        }
        
        item.setOrder(order);
        
        // Lấy user từ userId trong DTO nếu có
        if (dto.getUserId() != null) {
            User user = userRepo.findById(dto.getUserId()).orElse(null);
            if (user != null) {
                item.setUser(user);
            }
        }
        
        return item;
    }
    
    // Chuyển đổi OrderItem Entity -> OrderItemDTO
    public OrderItemDTO toDto(OrderItem entity) {
        if (entity == null) return null;
        
        Product product = entity.getProduct();
        User user = entity.getUser();
        
        return OrderItemDTO.builder()
                .id(entity.getId())
                .orderId(entity.getOrder() != null ? entity.getOrder().getId() : null)
                .productId(product != null ? product.getId() : null)
                .productName(product != null ? product.getName() : null)
                .productImageUrl(product != null ? product.getImageUrl() : null)
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .userId(user != null ? user.getId() : null)
                .userName(user != null ? user.getName() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
