package com.project2.ShoppingWeb.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.project2.ShoppingWeb.DTO.OrderDTO;
import com.project2.ShoppingWeb.DTO.OrderItemDTO;
import com.project2.ShoppingWeb.Service.OrderService;
import com.project2.ShoppingWeb.Enums.OrderStatus;
import com.project2.ShoppingWeb.Exception.NotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true", maxAge = 3600, 
    allowedHeaders = {"Origin", "Content-Type", "Accept", "Authorization"},
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<?> placeOrder(@RequestBody OrderDTO orderDTO) {
        try {
            log.info("Received order request: {}", orderDTO);
            
            // Kiểm tra xem orderItems có đầy đủ không
            if (orderDTO.getOrderItems() == null || orderDTO.getOrderItems().isEmpty()) {
                return ResponseEntity.badRequest().body("Order items cannot be empty");
            }
            
            // Kiểm tra dữ liệu trong từng orderItem
            for (OrderItemDTO item : orderDTO.getOrderItems()) {
                if (item.getProductId() == null) {
                    return ResponseEntity.badRequest().body("Product ID is required for each item");
                }
                log.debug("Processing item: {}", item);
            }
            
            // Kiểm tra nếu status là null thì gán giá trị mặc định
            if (orderDTO.getStatus() == null) {
                orderDTO.setStatus(OrderStatus.PENDING);
            }
            
            OrderDTO createdOrder = orderService.placeOrder(orderDTO);
            log.info("Order created successfully with ID: {}", createdOrder.getId());
            return ResponseEntity.ok(createdOrder);
        } catch (Exception e) {
            log.error("Error processing order: ", e);
            return ResponseEntity.status(500).body("Error processing order: " + e.getMessage());
        }
    }

    @PutMapping("/update-item-status/{orderItemId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateOrderItemStatus(@PathVariable Long orderItemId, @RequestParam String status) {
        try {
            OrderItemDTO updatedItem = orderService.updateOrderItemStatus(orderItemId, status);
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            log.error("Error updating order item status: ", e);
            return ResponseEntity.status(500).body("Error updating order item status: " + e.getMessage());
        }
    }
    
    @GetMapping("/order-status/{orderId}")
    public ResponseEntity<?> getOrderStatus(@PathVariable Long orderId) {
        try {
            log.info("REST request to get status for order ID: {}", orderId);
            
            OrderDTO order = orderService.getOrderById(orderId);
            
            // Đảm bảo chuyển enum thành String để tránh lỗi serialization
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("status", order.getStatus() != null ? order.getStatus().toString() : "UNKNOWN");
            response.put("paymentStatus", order.getPaymentStatus() != null ? order.getPaymentStatus().toString() : "UNKNOWN");
            response.put("orderId", order.getId());  // Thống nhất sử dụng orderId thay vì orderID
            response.put("lastUpdated", order.getUpdatedAt());
            
            log.info("Successfully retrieved order status: {}", response);
            return ResponseEntity.ok(response);
        } catch (NotFoundException e) {
            log.error("Order not found with ID: {}", orderId);
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "error", "Order not found",
                "status", "NOT_FOUND"
            ));
        } catch (Exception e) {
            log.error("Error getting order status: ", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "status", "ERROR"
            ));
        }
    }
    
    @GetMapping("/user")
    public ResponseEntity<?> getUserOrders() {
        try {
            return ResponseEntity.ok(orderService.getCurrentUserOrders());
        } catch (Exception e) {
            log.error("Error getting user orders: ", e);
            return ResponseEntity.status(500).body("Error getting user orders: " + e.getMessage());
        }
    }
    
    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        try {
            OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            log.error("Error updating order status: ", e);
            return ResponseEntity.status(500).body("Error updating order status: " + e.getMessage());
        }
    }

    @PutMapping("/{orderId}/payment-status")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long orderId, @RequestParam String status) {
        try {
            OrderDTO updatedOrder = orderService.updatePaymentStatus(orderId, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            log.error("Error updating payment status: ", e);
            return ResponseEntity.status(500).body("Error updating payment status: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@PathVariable String status) {
        try {
            log.info("REST request to get orders by status: {}", status);
            List<OrderDTO> orders = orderService.getOrdersByStatus(status);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Error getting orders by status: ", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.ok("Order deleted successfully");
    }   
} 