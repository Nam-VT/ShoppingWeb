package com.project2.ShoppingWeb.Mapper;

import java.math.BigDecimal;
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
import com.project2.ShoppingWeb.Repository.OrderRepo;
import com.project2.ShoppingWeb.Repository.ProductRepo;
import com.project2.ShoppingWeb.Repository.UserRepo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderMapper {
    
    private final ProductRepo productRepo;
    private final UserRepo userRepo;
    private final OrderRepo orderRepo;
    
    // Entity -> DTO
    public OrderDTO toDto(Order entity) {
        if (entity == null) return null;
        
        List<OrderItemDTO> orderItemsDto = null;
        if (entity.getOrderItems() != null) {
            orderItemsDto = entity.getOrderItems().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
        }
        
        return OrderDTO.builder()
                .id(entity.getId())
                .customerName(entity.getCustomerName())
                .customerEmail(entity.getCustomerEmail())
                .shippingAddress(entity.getShippingAddress())
                .totalPrice(entity.getTotalPrice())
                .orderItems(orderItemsDto)
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
        
        // Các trường ngày giờ thường được tạo tự động trong entity
        // nên không cần set từ DTO, trừ khi cần thiết
        
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
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + dto.getProductId()));
        }
        
        item.setProduct(product);
        item.setQuantity(dto.getQuantity());
        item.setPrice(dto.getPrice());
        item.setStatus(dto.getStatus() != null ? OrderStatus.valueOf(dto.getStatus()) : OrderStatus.PENDING);
        item.setOrder(order);
        
        return item;
    }
    
    // Chuyển đổi OrderItem Entity -> OrderItemDTO
    public OrderItemDTO toDto(OrderItem entity) {
        if (entity == null) return null;
        
        Product product = entity.getProduct();
        
        return OrderItemDTO.builder()
                .id(entity.getId())
                .productId(product != null ? product.getId() : null)
                .productName(product != null ? product.getName() : null)
                .productImageUrl(product != null ? product.getImageUrl() : null)
                .quantity(entity.getQuantity())
                .price(entity.getPrice())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .build();
    }
}
