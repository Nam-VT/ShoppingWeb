package com.project2.ShoppingWeb.Controller;

import com.project2.ShoppingWeb.Entity.Order;
import com.project2.ShoppingWeb.Entity.Payment;
import com.project2.ShoppingWeb.Service.OrderService;
import com.project2.ShoppingWeb.Service.PaymentService;
import com.project2.ShoppingWeb.Config.VNPayConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService vnPayService;
    private final OrderService orderService;
    private final VNPayConfig vnPayConfig;

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createPayment(@RequestBody Map<String, String> paymentRequest) {
        try {
            // Kiểm tra xem URL callback đã được cấu hình chưa
            if (!vnPayConfig.isReturnUrlConfigured()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Callback URL not configured. Please update through /api/config/update-callback first.");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Lấy thông tin thanh toán từ request
            String orderInfo = paymentRequest.getOrDefault("orderInfo", "Payment for order");
            long amount;
            try {
                amount = Long.parseLong(paymentRequest.getOrDefault("amount", "0"));
            } catch (NumberFormatException e) {
                amount = 0;
            }
            String orderType = paymentRequest.getOrDefault("orderType", "other");
            
            // Tạo mã đơn hàng ngẫu nhiên
            String txnRef = String.valueOf(System.currentTimeMillis());
            
            // Tạo URL thanh toán VNPAY
            String paymentUrl = createVnPayUrl(txnRef, amount, orderInfo, orderType);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("paymentUrl", paymentUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // In ra toàn bộ stack trace
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error: " + e.getMessage());
            errorResponse.put("detail", e.toString());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    private String createVnPayUrl(String txnRef, long amount, String orderInfo, String orderType) {
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVnp_Version());
        vnpParams.put("vnp_Command", vnPayConfig.getVnp_Command());
        vnpParams.put("vnp_TmnCode", vnPayConfig.getVnp_TmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // Nhân với 100 vì VNPay yêu cầu số tiền * 100
        vnpParams.put("vnp_CurrCode", vnPayConfig.getVnp_CurrCode());
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", orderType);
        vnpParams.put("vnp_Locale", vnPayConfig.getVnp_Locale());
        
        // Sử dụng URL callback động đã được cấu hình
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
        
        // Thêm thời gian tạo giao dịch
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(new Date());
        vnpParams.put("vnp_CreateDate", createDate);
        
        // Thêm IP của khách hàng (tùy chọn)
        vnpParams.put("vnp_IpAddr", "127.0.0.1"); // Trong thực tế, lấy IP từ request
        
        // Sắp xếp các tham số theo thứ tự a-z trước khi tạo chữ ký
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        
        // Tạo chuỗi dữ liệu để ký
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Tạo chuỗi dữ liệu để ký
                hashData.append(fieldName).append('=')
                    .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8)).append('&');
                
                // Tạo chuỗi query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8)).append('=')
                    .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8)).append('&');
            }
        }
        
        // Xóa dấu & ở cuối chuỗi
        if (hashData.length() > 0) {
            hashData.setLength(hashData.length() - 1);
        }
        
        // Tạo chữ ký - sử dụng service
        String secureHash = vnPayService.hmacSHA512(vnPayConfig.getVnp_HashSecret(), hashData.toString());
        
        // Thêm chữ ký vào URL
        query.append("vnp_SecureHash=").append(secureHash);
        
        // Tạo URL thanh toán hoàn chỉnh
        return vnPayConfig.getVnp_Url() + "?" + query.toString();
    }
    
    @GetMapping("/vnpay-return")
    public String handleVnPayReturn(@RequestParam Map<String, String> params) {
        // Xử lý callback từ VNPAY
        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        
        // Kiểm tra chữ ký trả về
        if (isValidCallback(params)) {
            if ("00".equals(vnp_ResponseCode)) {
                // Thanh toán thành công
                return "Thanh toán thành công!";
            } else {
                // Thanh toán thất bại
                return "Thanh toán thất bại: " + getResponseDescription(vnp_ResponseCode);
            }
        } else {
            return "Invalid callback signature!";
        }
    }
    
    private boolean isValidCallback(Map<String, String> params) {
        // TODO: Implement signature validation for callback
        // Tạm thời return true, trong thực tế cần xác thực chữ ký
        return true;
    }
    
    private String getResponseDescription(String responseCode) {
        // Mô tả các mã phản hồi từ VNPAY
        Map<String, String> responseDescriptions = new HashMap<>();
        responseDescriptions.put("00", "Giao dịch thành công");
        responseDescriptions.put("07", "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường)");
        responseDescriptions.put("09", "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng");
        // ... Thêm các mã khác
        
        return responseDescriptions.getOrDefault(responseCode, "Mã lỗi không xác định");
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

    // Thêm phương thức test để debug
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testPayment() {
        Map<String, String> response = new HashMap<>();
        try {
            response.put("status", "success");
            response.put("message", "Payment endpoint is working");
            response.put("returnUrl", vnPayConfig.getVnp_ReturnUrl());
            response.put("vnpVersion", vnPayConfig.getVnp_Version());
            response.put("vnpCommand", vnPayConfig.getVnp_Command());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
