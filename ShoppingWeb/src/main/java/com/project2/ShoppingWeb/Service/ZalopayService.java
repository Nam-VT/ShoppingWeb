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

import lombok.RequiredArgsConstructor;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ZalopayService {
    
    private final ZalopayConfig zalopayConfig;

    private String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }

    public String createOrder(Map<String, Object> orderRequest) {
        try {
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
}
