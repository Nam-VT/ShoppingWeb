package com.project2.ShoppingWeb.Service;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.stereotype.Service;

import com.project2.ShoppingWeb.Config.ZalopayConfig;
import com.project2.ShoppingWeb.Crypto.HMACUtil;
import com.project2.ShoppingWeb.Entity.Order;
import com.project2.ShoppingWeb.Repository.OrderRepo;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ZalopayService {
    
    private final ZalopayConfig zalopayConfig;
    private final OrderRepo orderRepo;
    
    private String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }

    public String createOrder(Map<String, Object> orderRequest) {
        try {
            // Lấy orderId nếu có
            Long orderId = null;
            if (orderRequest.containsKey("orderId")) {
                try {
                    orderId = Long.valueOf(orderRequest.get("orderId").toString());
                    System.out.println("Received orderID: " + orderId);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid orderId format: " + orderRequest.get("orderId"));
                }
            }
            
            // Sử dụng app_trans_id từ request nếu có, nếu không thì tạo mới
            String appTransId;
            if (orderRequest.containsKey("app_trans_id")) {
                appTransId = orderRequest.get("app_trans_id").toString();
            } else {
                Random rand = new Random();
                int randomId = rand.nextInt(1000000);
                appTransId = getCurrentTimeString("yyMMdd") + "_" + randomId;
            }
            
            Object amount = orderRequest.get("amount");
            if (amount == null) {
                return "{\"error\": \"Amount is required\"}";
            }

            Map<String, Object> order = new HashMap<>();
            order.put("app_id", zalopayConfig.getAppId());
            order.put("app_trans_id", appTransId);
            order.put("app_time", System.currentTimeMillis());
            order.put("app_user", "user123");
            order.put("amount", amount);
            order.put("description", "SN Mobile - Payment for the order #" + appTransId);
            order.put("bank_code", "");
            order.put("item", "[{}]");
            order.put("embed_data", "{}");
            order.put("callback_url", zalopayConfig.getCallbackUrl());

            String data = order.get("app_id") + "|" + order.get("app_trans_id") + "|" + order.get("app_user") + "|"
                    + order.get("amount") + "|" + order.get("app_time") + "|" + order.get("embed_data") + "|"
                    + order.get("item");

            String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, zalopayConfig.getKey1(), data);
            order.put("mac", mac);

            System.out.println("Generated MAC: " + mac);

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(zalopayConfig.getEndpoint());

                List<NameValuePair> params = new ArrayList<>();
                for (Map.Entry<String, Object> entry : order.entrySet()) {
                    params.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
                }

                post.setEntity(new UrlEncodedFormEntity(params));

                try (CloseableHttpResponse response = client.execute(post)) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    StringBuilder resultJsonStr = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        resultJsonStr.append(line);
                    }

                    System.out.println("Zalopay Response: " + resultJsonStr.toString());
                    
                    // Kiểm tra response từ ZaloPay
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> responseData = objectMapper.readValue(resultJsonStr.toString(), Map.class);
                    
                    // Nếu tạo order thành công và có orderId, cập nhật transactionId
                    if (responseData.containsKey("return_code") && 
                        Integer.parseInt(responseData.get("return_code").toString()) == 1 && 
                        orderId != null) {
                        
                        updateOrderTransactionId(orderId, appTransId);
                    }

                    return resultJsonStr.toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to create order: " + e.getMessage() + "\"}";
        }
    }

    public String getOrderStatus(String appTransId) {
        String data = zalopayConfig.getAppId() + "|" + appTransId + "|" + zalopayConfig.getKey1();
        String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, zalopayConfig.getKey1(), data);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(zalopayConfig.getOrderStatusEndpoint());

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("app_id", zalopayConfig.getAppId()));
            params.add(new BasicNameValuePair("app_trans_id", appTransId));
            params.add(new BasicNameValuePair("mac", mac));

            post.setEntity(new UrlEncodedFormEntity(params));

            try (CloseableHttpResponse response = client.execute(post)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                StringBuilder resultJsonStr = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    resultJsonStr.append(line);
                }

                return resultJsonStr.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Failed to get order status: " + e.getMessage() + "\"}";
        }
    }
    
    /**
     * Cập nhật transactionId cho đơn hàng
     * @param orderId ID của đơn hàng
     * @param transactionId ID giao dịch từ ZaloPay (app_trans_id)
     * @return true nếu cập nhật thành công, false nếu không
     */
    public boolean updateOrderTransactionId(Long orderId, String transactionId) {
        try {
            Optional<Order> orderOpt = orderRepo.findById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                order.setTransactionId(transactionId);
                orderRepo.save(order);
                System.out.println("Updated transaction ID for order " + orderId + ": " + transactionId);
                return true;
            } else {
                System.out.println("Order not found with ID: " + orderId);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
