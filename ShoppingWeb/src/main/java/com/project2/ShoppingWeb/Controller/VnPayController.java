package com.project2.ShoppingWeb.Controller;

import com.project2.ShoppingWeb.Service.VnPayService;
import com.project2.ShoppingWeb.Config.VNPayConfig;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Enumeration;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class VnPayController {

    private final VnPayService vnPayService;
    private final VNPayConfig vnPayConfig;

    @GetMapping("/create")
    public ResponseEntity<Map<String, String>> createPayment(
            HttpServletRequest request,
            @RequestBody Map<String, String> paymentRequest) {
        try {
            String orderInfo = paymentRequest.getOrDefault("orderInfo", "Payment for order");
            long amount = Long.parseLong(paymentRequest.getOrDefault("amount", "0"));
            
            // Sử dụng service
            String paymentUrl = vnPayService.createPaymentUrl(request, amount, orderInfo);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("paymentUrl", paymentUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Error: " + e.getMessage());
            errorResponse.put("detail", e.toString());
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/pay")
    public String getPay() throws UnsupportedEncodingException {
        // Sử dụng giá trị từ config
        String vnp_Version = vnPayConfig.getVnp_Version();
        String vnp_Command = vnPayConfig.getVnp_Command();
        String orderType = vnPayConfig.getVnp_OrderType();
        long amount = 10000*100;
        String bankCode = "NCB";
        
        // Tạo mã giao dịch ngẫu nhiên sử dụng phương thức từ config
        String vnp_TxnRef = vnPayConfig.getRandomNumber(8);
        String vnp_IpAddr = "127.0.0.1";
        
        String vnp_TmnCode = vnPayConfig.getVnp_TmnCode();
        
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", vnPayConfig.getVnp_CurrCode());
        
        vnp_Params.put("vnp_BankCode", bankCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
        vnp_Params.put("vnp_OrderType", orderType);
        
        vnp_Params.put("vnp_Locale", vnPayConfig.getVnp_Locale());
        vnp_Params.put("vnp_ReturnUrl", vnPayConfig.getVnp_ReturnUrl());
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
        
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                
                if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        
        String queryUrl = query.toString();
        // Sử dụng phương thức từ VNPayConfig thay vì từ service
        String vnp_SecureHash = VNPayConfig.hmacSHA512(vnPayConfig.getVnp_HashSecret(), hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = vnPayConfig.getVnp_PayUrl() + "?" + queryUrl;
        
        return paymentUrl;
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

    @GetMapping("/debug")
    public ResponseEntity<Map<String, Object>> debugVnPay() {
        Map<String, Object> debug = new HashMap<>();
        
        debug.put("service_tmnCode", vnPayConfig.getVnp_TmnCode());
        debug.put("config_tmnCode", vnPayConfig.getVnp_TmnCode());
        
        debug.put("service_url", vnPayConfig.getVnp_PayUrl());
        debug.put("config_url", vnPayConfig.getVnp_PayUrl());
        
        debug.put("service_returnUrl", vnPayConfig.getVnp_ReturnUrl());
        debug.put("config_returnUrl", vnPayConfig.getVnp_ReturnUrl());
        
        // Test tạo hash sử dụng phương thức từ VNPayConfig
        String testData = "test=data";
        String hashFromConfig = VNPayConfig.hmacSHA512(vnPayConfig.getVnp_HashSecret(), testData);
        
        debug.put("hashFromConfig", hashFromConfig);
        
        return ResponseEntity.ok(debug);
    }

    @PostMapping("/ipn-url")
    public ResponseEntity<Map<String, String>> handleIpnCallback(HttpServletRequest request) {
        try {
            // Bước 1: Thu thập tất cả tham số từ request
            Map<String, String> fields = new HashMap<>();
            for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements();) {
                String fieldName = params.nextElement();
                String fieldValue = request.getParameter(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    fields.put(fieldName, URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                }
            }

            // Bước 2: Trích xuất và loại bỏ chữ ký bảo mật
            String vnp_SecureHash = request.getParameter("vnp_SecureHash");
            if (fields.containsKey("vnp_SecureHashType")) {
                fields.remove("vnp_SecureHashType");
            }
            if (fields.containsKey("vnp_SecureHash")) {
                fields.remove("vnp_SecureHash");
            }

            // Bước 3: Tính toán và xác thực giá trị hash
            String signValue = hashAllFields(fields);
            Map<String, String> response = new HashMap<>();

            if (signValue.equals(vnp_SecureHash)) {
                // Bước 4: Kiểm tra sự tồn tại, số tiền và trạng thái của đơn hàng
                // Phần này cần được thay thế bằng logic thực tế từ cơ sở dữ liệu của bạn
                boolean checkOrderId = true; // Kiểm tra vnp_TxnRef tồn tại trong cơ sở dữ liệu
                boolean checkAmount = true; // Kiểm tra vnp_Amount khớp với số tiền trong cơ sở dữ liệu
                boolean checkOrderStatus = true; // Kiểm tra trạng thái thanh toán đang chờ xử lý (0)

                if (checkOrderId) {
                    if (checkAmount) {
                        if (checkOrderStatus) {
                            if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
                                // Thanh toán thành công - cập nhật cơ sở dữ liệu sang trạng thái 1
                                // TODO: Thêm mã cập nhật cơ sở dữ liệu ở đây
                            } else {
                                // Thanh toán thất bại - cập nhật cơ sở dữ liệu sang trạng thái 2
                                // TODO: Thêm mã cập nhật cơ sở dữ liệu ở đây
                            }
                            response.put("RspCode", "00");
                            response.put("Message", "Confirm Success");
                        } else {
                            response.put("RspCode", "02");
                            response.put("Message", "Order already confirmed");
                        }
                    } else {
                        response.put("RspCode", "04");
                        response.put("Message", "Invalid Amount");
                    }
                } else {
                    response.put("RspCode", "01");
                    response.put("Message", "Order not Found");
                }
            } else {
                response.put("RspCode", "97");
                response.put("Message", "Invalid Checksum");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("RspCode", "99");
            errorResponse.put("Message", "Unknown error");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Phương thức hỗ trợ để tính toán hash từ tất cả các trường
    private String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append("=");
                hashData.append(fieldValue);
                
                if (itr.hasNext()) {
                    hashData.append("&");
                }
            }
        }
        
        return VNPayConfig.hmacSHA512(vnPayConfig.getVnp_HashSecret(), hashData.toString());
    }
}
