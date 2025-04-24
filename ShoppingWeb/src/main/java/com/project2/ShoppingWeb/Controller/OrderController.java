package com.project2.ShoppingWeb.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.project2.ShoppingWeb.DTO.OrderDTO;
import com.project2.ShoppingWeb.DTO.OrderItemDTO;
import com.project2.ShoppingWeb.Service.OrderService;
import com.project2.ShoppingWeb.Enums.OrderStatus;

import lombok.RequiredArgsConstructor;
import java.util.Arrays;

@RestController
@RequestMapping("/order")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderItemService;

    @PostMapping("/create")
    public ResponseEntity<?> placeOrder(@RequestBody OrderDTO orderDTO) {
        try {
            // Kiểm tra nếu status là null thì gán giá trị mặc định
            if (orderDTO.getStatus() == null) {
                orderDTO.setStatus(OrderStatus.PENDING);
            }
            
            return ResponseEntity.ok(orderItemService.placeOrder(orderDTO));
        } catch (Exception e) {
            // Log lỗi cho dễ debug
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error processing order: " + e.getMessage());
        }
    }

    @PutMapping("/update-item-status/{orderItemId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<OrderItemDTO> updateOrderItemStatus(@PathVariable Long orderItemId, @RequestParam String status){
        return ResponseEntity.ok(orderItemService.updateOrderItemStatus(orderItemId, status));
    }
    
    // Thêm API endpoint mới để kiểm tra trạng thái đơn hàng
    @GetMapping("/{orderId}/status")
    public ResponseEntity<OrderDTO> getOrderStatus(@PathVariable Long orderId) {
        OrderDTO order = orderItemService.getOrderById(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }
    
    // Lấy danh sách đơn hàng của người dùng hiện tại
    @GetMapping("/user")
    public ResponseEntity<?> getUserOrders() {
        return ResponseEntity.ok(orderItemService.getCurrentUserOrders());
    }
    
    // Cập nhật trạng thái đơn hàng (toàn bộ đơn hàng)
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        return ResponseEntity.ok(orderItemService.updateOrderStatus(orderId, status));
    }

    // Thêm endpoint cập nhật trạng thái thanh toán
    @PutMapping("/{orderId}/payment-status")
    public ResponseEntity<OrderDTO> updatePaymentStatus(@PathVariable Long orderId, @RequestParam String status) {
        OrderDTO updatedOrder = orderItemService.updatePaymentStatus(orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }
} 