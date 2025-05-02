package com.project2.ShoppingWeb.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.project2.ShoppingWeb.DTO.OrderItemDTO;
import com.project2.ShoppingWeb.Service.OrderItemService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/orderItem")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @PutMapping("/update-item-status/{orderItemId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> updateOrderItemStatus(@PathVariable Long orderItemId, @RequestParam String status) {
        try {
            OrderItemDTO updatedItem = orderItemService.updateOrderItemStatus(orderItemId, status);
            return ResponseEntity.ok(updatedItem);
        } catch (Exception e) {
            log.error("Error updating order item status: ", e);
            return ResponseEntity.status(500).body("Error updating order item status: " + e.getMessage());
        }
    }
    
    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<?> getOrderItemsByOrderId(@PathVariable Long orderId) {
        try {
            return ResponseEntity.ok(orderItemService.getOrderItemsByOrderId(orderId));
        } catch (Exception e) {
            log.error("Error fetching order items: ", e);
            return ResponseEntity.status(500).body("Error fetching order items: " + e.getMessage());
        }
    }
} 