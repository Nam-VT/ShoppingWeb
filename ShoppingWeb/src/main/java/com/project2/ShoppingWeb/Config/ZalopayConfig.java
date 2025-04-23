package com.project2.ShoppingWeb.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class ZalopayConfig {

    @Value("${zalopay.app-id}")
    private String appId;
    
    @Value("${zalopay.key1}")
    private String key1;
    
    @Value("${zalopay.key2}")
    private String key2;
    
    @Value("${zalopay.endpoint}")
    private String endpoint;
    
    @Value("${zalopay.orderstatus}")
    private String orderStatusEndpoint;
    
    @Value("${zalopay.callback-url}")
    private String callbackUrl;
    
    // Nếu cần thêm các phương thức tiện ích
    public static String hmacSHA256(final String key, final String data) {
        try {
            // Triển khai phương thức HMAC nếu cần
            // Hoặc sử dụng HMACUtil class hiện có
            return com.project2.ShoppingWeb.Crypto.HMACUtil.HMacHexStringEncode(
                com.project2.ShoppingWeb.Crypto.HMACUtil.HMACSHA256, key, data);
        } catch (Exception e) {
            return "";
        }
    }

    // Thêm các phương thức getter dưới đây nếu Lombok không hoạt động
    public String getAppId() {
        return appId;
    }

    public String getKey1() {
        return key1;
    }

    public String getKey2() {
        return key2;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getOrderStatusEndpoint() {
        return orderStatusEndpoint;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }
}
