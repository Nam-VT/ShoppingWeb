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
import com.project2.ShoppingWeb.Enums.PaymentStatus;
import com.project2.ShoppingWeb.Exception.NotFoundException;
import com.project2.ShoppingWeb.Mapper.OrderMapper;
import com.project2.ShoppingWeb.Repository.OrderItemRepo;
import com.project2.ShoppingWeb.Repository.OrderRepo;
import com.project2.ShoppingWeb.Repository.ProductRepo;
import com.project2.ShoppingWeb.Service.OrderService;
import com.project2.ShoppingWeb.Service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final ProductRepo productRepo;
    private final UserService userService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO placeOrder(OrderDTO orderDTO) {
        try {
            User user = userService.getLoginUser();
            
            // Tạo đơn hàng mới
            Order order = new Order();
            order.setCustomerName(user.getName());
            order.setCustomerEmail(user.getEmail());
            order.setShippingAddress(orderDTO.getShippingAddress());
            order.setTotalPrice(orderDTO.getTotalPrice());
            order.setPaymentMethod(orderDTO.getPaymentMethod());
            
            // Đảm bảo có status cho Order
            if (orderDTO.getStatus() == null) {
                order.setStatus(OrderStatus.PENDING); // Giá trị mặc định nếu null
            } else {
                order.setStatus(orderDTO.getStatus()); // Gán trực tiếp vì đã là enum OrderStatus
            }
            
            // Đặt paymentStatus
            if (orderDTO.getPaymentStatus() != null) {
                order.setPaymentStatus(orderDTO.getPaymentStatus());
            } else if ("ZALOPAY".equals(orderDTO.getPaymentMethod()) || "VNPAY".equals(orderDTO.getPaymentMethod())) {
                order.setStatus(OrderStatus.PROCESSING);
                order.setPaymentStatus(PaymentStatus.PROCESSING);
            } else {
                order.setStatus(OrderStatus.PENDING);
                order.setPaymentStatus(PaymentStatus.PENDING);
            }
            
            // Xử lý các item
            List<OrderItem> orderItems = new ArrayList<>();
            
            for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
                Product product = productRepo.findById(itemDTO.getProductId())
                        .orElseThrow(() -> new NotFoundException("Product not found with id: " + itemDTO.getProductId()));
                
                OrderItem item = new OrderItem();
                item.setProduct(product);
                item.setQuantity(itemDTO.getQuantity());
                item.setPrice(itemDTO.getPrice());
                item.setUser(user);
                item.setOrder(order);
                
                // Quan trọng: Đảm bảo OrderItem có status
                item.setStatus(order.getStatus()); // Lấy status từ Order
                
                orderItems.add(item);
            }
            
            order.setOrderItems(orderItems);
            order.setUser(user);
            
            // Lưu đơn hàng
            Order savedOrder = orderRepo.save(order);
            
            // Chuyển sang DTO và trả về
            return orderMapper.toDto(savedOrder);
        } catch (Exception e) {
            // Log lỗi chi tiết
            log.error("Error creating order", e);
            throw e; // Sẽ tự động rollback transaction
        }
    }

    @Override
    public OrderItemDTO updateOrderItemStatus(Long orderItemId, String status) {
        // Vì OrderItem không có trường status, chúng ta sẽ cập nhật trạng thái của đơn hàng chứa item này
        OrderItem orderItem = orderItemRepo.findById(orderItemId)
                .orElseThrow(() -> new NotFoundException("Order Item not found"));

        Order order = orderItem.getOrder();
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(orderStatus);
            orderRepo.save(order);
            
            // Trả về OrderItemDTO đã cập nhật
            return orderMapper.toDto(orderItem);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }

    @Override
    public OrderDTO getOrderById(Long orderId) {
        log.info("Getting order by ID: {}", orderId);
        
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with ID: {}", orderId);
                    return new NotFoundException("Order not found with id: " + orderId);
                });
        
        log.info("Found order: ID={}, Status={}, PaymentStatus={}", 
                 order.getId(), order.getStatus(), order.getPaymentStatus());
        
        return orderMapper.toDto(order);
    }

    @Override
    public List<OrderDTO> getCurrentUserOrders() {
        User user = userService.getLoginUser();
        List<Order> orders = orderRepo.findByUserId(user.getId());
        return orders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String status) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with id: " + orderId));
        
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(orderStatus);
            Order updatedOrder = orderRepo.save(order);
            return orderMapper.toDto(updatedOrder);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }
    
    @Override
    @Transactional
    public OrderDTO updatePaymentStatus(Long orderId, String status) {
        try {
            log.info("Updating payment status for order {}: {}", orderId, status);
            
            Order order = orderRepo.findById(orderId)
                    .orElseThrow(() -> {
                        log.error("Order not found for payment update: {}", orderId);
                        return new NotFoundException("Order not found with id: " + orderId);
                    });
            
            // Xử lý trường hợp status không hợp lệ
            PaymentStatus paymentStatus;
            try {
                paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
                log.info("Converted status string '{}' to enum: {}", status, paymentStatus);
            } catch (IllegalArgumentException e) {
                log.error("Invalid payment status: {}, falling back to FAILED", status);
                paymentStatus = PaymentStatus.FAILED;
            }
            
            order.setPaymentStatus(paymentStatus);
            
            // Nếu thanh toán thành công, cập nhật trạng thái đơn hàng
            if (paymentStatus == PaymentStatus.PAID) {
                log.info("Payment PAID, updating order status to CONFIRMED");
                order.setStatus(OrderStatus.CONFIRMED);
            }
            // Nếu thanh toán thất bại, cập nhật trạng thái đơn hàng 
            else if (paymentStatus == PaymentStatus.FAILED) {
                log.info("Payment FAILED, updating order status to CANCELLED");
                order.setStatus(OrderStatus.CANCELLED);
            }
            
            Order updatedOrder = orderRepo.save(order);
            log.info("Order payment status updated: ID={}, Status={}, PaymentStatus={}",
                    updatedOrder.getId(), updatedOrder.getStatus(), updatedOrder.getPaymentStatus());
            
            return orderMapper.toDto(updatedOrder);
        } catch (Exception e) {
            log.error("Error updating payment status: ", e);
            throw e;
        }
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepo.findAll();
        return orders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(String status) {
        try {
            log.info("Finding orders with status: {}", status);
            
            // Chuyển đổi String thành enum OrderStatus
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            List<Order> orders = orderRepo.findByStatus(orderStatus);
            
            log.info("Found {} orders with status {}", orders.size(), orderStatus);
            return orders.stream()
                    .map(orderMapper::toDto)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            log.error("Invalid order status: {}. Error: {}", status, e.getMessage());
            return new ArrayList<>(); // Trả về danh sách rỗng nếu status không hợp lệ
        }
    }

    @Override
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepo.findByUserId(userId);
        return orders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOrder(Long orderId) {
        orderRepo.deleteById(orderId);
    }
}
