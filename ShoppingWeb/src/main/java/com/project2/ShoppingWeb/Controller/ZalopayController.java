package com.project2.ShoppingWeb.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project2.ShoppingWeb.Service.ZalopayService;
import com.project2.ShoppingWeb.Config.ZalopayConfig;
import com.project2.ShoppingWeb.Crypto.HMACUtil;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("zalopay")
@RequiredArgsConstructor
public class ZalopayController {

    private final ZalopayService zalopayService;
    private final ZalopayConfig zalopayConfig;

    @PostMapping("/create-order")
    public ResponseEntity<Object> createOrder(@RequestBody Map<String, Object> orderRequest) {
        try {
            // Lấy app_trans_id trước khi gọi service
            String appTransId = getCurrentTimeString("yyMMdd") + "_" + new Random().nextInt(1000000);
            orderRequest.put("app_trans_id", appTransId);
            
            String result = zalopayService.createOrder(orderRequest);
            
            // Tạo response mới bao gồm cả app_trans_id để client có thể lưu trữ
            Map<String, Object> response = new HashMap<>();
            response.put("zalopay_response", result);
            response.put("app_trans_id", appTransId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/order-status/{appTransId}")
    public ResponseEntity<Object> getOrderStatus(@PathVariable String appTransId) {
        try {
            String result = zalopayService.getOrderStatus(appTransId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<Map<String, Object>> handleCallback(@RequestBody Map<String, Object> cbdata) {
        Map<String, Object> result = new HashMap<>();

        try {
            String dataStr = cbdata.get("data").toString();
            String reqMac = cbdata.get("mac").toString();

            // Tính toán MAC sử dụng key2 từ cấu hình
            String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, zalopayConfig.getKey2(), dataStr);

            System.out.println("Received MAC: " + reqMac);
            System.out.println("Calculated MAC: " + mac);

            // Kiểm tra callback hợp lệ (đến từ ZaloPay server)
            if (!reqMac.equals(mac)) {
                // Callback không hợp lệ
                result.put("return_code", -1);
                result.put("return_message", "mac not equal");
            } else {
                // Thanh toán thành công
                // Merchant cập nhật trạng thái cho đơn hàng
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> dataJson = objectMapper.readValue(dataStr, Map.class);
                String appTransId = dataJson.get("app_trans_id").toString();
                
                System.out.println("Updating order status for app_trans_id = " + appTransId);
                
                // TODO: Cập nhật trạng thái đơn hàng trong database của bạn
                // orderService.updateOrderStatus(appTransId, "SUCCESS");

                result.put("return_code", 1);
                result.put("return_message", "success");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            result.put("return_code", 0); // ZaloPay server sẽ callback lại (tối đa 3 lần)
            result.put("return_message", ex.getMessage());
        }

        // Thông báo kết quả cho ZaloPay server
        return ResponseEntity.ok(result);
    }

    // Thêm phương thức hỗ trợ
    private String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }
}
