package com.project2.ShoppingWeb.Controller;

import com.project2.ShoppingWeb.Entity.Order;
import com.project2.ShoppingWeb.Entity.Payment;
import lombok.RequiredArgsConstructor;
import com.project2.ShoppingWeb.Service.OrderService;
import com.project2.ShoppingWeb.Service.PaymentService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService vnPayService;
    
    private final OrderService orderService;
    
    @Value("${vnpay.hash-secret}")
    private String vnp_HashSecret;

    @PostMapping("/create")
    public ResponseEntity<String> createPayment(@RequestParam Long amount, @RequestParam Long orderId) {
        // Lấy thông tin đơn hàng từ OrderItemService
        Order order = orderService.getOrderById(orderId);

        // Tạo URL thanh toán
        String paymentUrl = vnPayService.createPaymentUrl(amount, orderId.toString(), "Thanh toán đơn hàng #" + orderId);

        return ResponseEntity.ok(paymentUrl);
    }


    @GetMapping("/vnpay-return")
    public String vnpayReturn(@RequestParam Map<String, String> response) {
        // Xác thực chữ ký
        String vnpSecureHash = response.get("vnp_SecureHash");
        
        // Tạo lại chữ ký để so sánh
        Map<String, String> vnpParams = new HashMap<>();
        for (Map.Entry<String, String> entry : response.entrySet()) {
            if (!entry.getKey().equals("vnp_SecureHash") && !entry.getKey().equals("vnp_SecureHashType")) {
                vnpParams.put(entry.getKey(), entry.getValue());
            }
        }
        
        // Tạo chuỗi hash data
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(fieldValue).append('&');
            }
        }
        String data = hashData.toString();
        if (data.endsWith("&")) {
            data = data.substring(0, data.length() - 1);
        }
        
        // Tính toán chữ ký
        String calculatedHash = vnPayService.hmacSHA512(vnp_HashSecret, data);
        
        // So sánh chữ ký
        if (!calculatedHash.equals(vnpSecureHash)) {
            return "Invalid signature";
        }
        
        String responseCode = response.get("vnp_ResponseCode");

        if ("00".equals(responseCode)) { // Thành công
            String orderId = response.get("vnp_TxnRef");
            Order order = orderService.getOrderById(Long.valueOf(orderId));
            
            // Lưu thông tin giao dịch thành công
            // Điều chỉnh số tiền bằng cách chia cho 100
            BigDecimal amount = BigDecimal.valueOf(Long.parseLong(response.get("vnp_Amount")) / 100.0);
            vnPayService.savePayment(order, amount);

            return "Thanh toán thành công! Cảm ơn bạn đã mua hàng.";
        } else {
            return "Thanh toán thất bại. Vui lòng thử lại!";
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Optional<List<Payment>> getAllPayments() {
        return vnPayService.getAllPayments();
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public Optional<Payment> searchPaymentByOrderId(@RequestParam Long orderId) {
        return vnPayService.getPaymentByOrderId(orderId);
    }
}
