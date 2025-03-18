package com.project2.ShoppingWeb.Service.ServiceImpl;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.project2.ShoppingWeb.Repository.OrederItemRepo;
import com.project2.ShoppingWeb.Service.OrderItemService;
import com.project2.ShoppingWeb.DTO.OrderRequest;
import com.project2.ShoppingWeb.Entity.OrderItem;
import com.project2.ShoppingWeb.Entity.Product;
import com.project2.ShoppingWeb.Enums.OrderStatus;
import com.project2.ShoppingWeb.Exception.NotFoundException;
import  com.project2.ShoppingWeb.Repository.ProductRepo;
import com.project2.ShoppingWeb.Repository.UserRepo;
import com.project2.ShoppingWeb.Repository.OrderRepo;
import com.project2.ShoppingWeb.Entity.User;
import com.project2.ShoppingWeb.Service.UserService;
import com.project2.ShoppingWeb.Entity.Order;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    OrederItemRepo orderItemRepo;
    OrderRepo orderRepo;
    ProductRepo productRepo;
    UserService userService;
    UserRepo userRepo;

    @Override
    public Order placeOrder(OrderRequest orderRequest) {
        User user = userService.getLoginUser();

        // Kiểm tra nếu danh sách mục hàng trống
        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order request must contain at least one item.");
        }

        List<OrderItem> orderItems = orderRequest.getItems().stream().map(orderItemRequest -> {
            Product product = productRepo.findById(orderItemRequest.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product Not Found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(orderItemRequest.getQuantity());
            orderItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(orderItemRequest.getQuantity())));
            orderItem.setStatus(OrderStatus.PENDING);
            orderItem.setUser(user);
            return orderItem;
        }).collect(Collectors.toList());

        BigDecimal totalPrice = orderItems.stream()
                .map(OrderItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setOrderItems(orderItems);
        order.setTotalPrice(totalPrice);

        // Liên kết các mục hàng với đơn hàng
        orderItems.forEach(orderItem -> orderItem.setOrder(order));

        // Lưu đơn hàng và các mục hàng vào cơ sở dữ liệu
        orderRepo.save(order);

        return order; // Trả về đơn hàng đã được tạo thành công
    }


    @Override
    public Order updateOrderItemStatus(Long orderItemId, String status) {
        OrderItem orderItem = orderItemRepo.findById(orderItemId)
                .orElseThrow(() -> new NotFoundException("Order Item Not Found"));

        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            orderItem.setStatus(orderStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }

        orderItemRepo.save(orderItem);

        return orderItem.getOrder();
    }


    @Override
    public List<OrderItem> filterOrderItems(OrderStatus status, LocalDateTime startDate,
                                            Long itemId) {

        List<OrderItem> allOrderItems = orderItemRepo.findAll(); // Lấy toàn bộ dữ liệu

        return allOrderItems.stream()
                .filter(orderItem -> status == null || orderItem.getStatus() == status)
                .filter(orderItem -> startDate == null || !orderItem.getCreatedAt().isBefore(startDate))
                .filter(orderItem -> itemId == null || orderItem.getId().equals(itemId))
                .collect(Collectors.toList());
    }
}
