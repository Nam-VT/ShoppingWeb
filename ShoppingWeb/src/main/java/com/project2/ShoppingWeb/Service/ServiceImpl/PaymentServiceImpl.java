package com.project2.ShoppingWeb.Service.ServiceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

import com.project2.ShoppingWeb.Service.PaymentService;
import com.project2.ShoppingWeb.Entity.Order;
import com.project2.ShoppingWeb.Entity.Payment;
import com.project2.ShoppingWeb.Enums.PaymentMethod;
import com.project2.ShoppingWeb.Enums.PaymentStatus;
import com.project2.ShoppingWeb.Repository.PaymentRepo;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${vnpay.tmn-code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnp_HashSecret;

    @Value("${vnpay.url}")
    private String vnp_Url;

    @Value("${vnpay.return-url}")
    private String vnp_ReturnUrl;

    PaymentRepo paymentRepo;

    @Override
    public String createPaymentUrl(Long amount, String orderId, String orderInfo) {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf(amount * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", orderId);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        params.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        // Sắp xếp các tham số theo thứ tự alphabet
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8))
                     .append('=')
                     .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8))
                     .append('&');
                hashData.append(fieldName).append('=').append(fieldValue).append('&');
            }
        }

        String queryUrl = query.toString().substring(0, query.length() - 1);
        String secureHash = hmacSHA512(vnp_HashSecret, hashData.toString().substring(0, hashData.length() - 1));
        return vnp_Url + "?" + queryUrl + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public String hmacSHA512(String key, String data) {
        try {
            Mac hmacSha512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmacSha512.init(secretKeySpec);
            byte[] hmacData = hmacSha512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmacData);
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA-512", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public Payment savePayment(Order order, BigDecimal amount) {
        Payment payment = Payment.builder()
                .order(order)
                .method(PaymentMethod.VNPAY)
                .status(PaymentStatus.CONFIRMED)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return paymentRepo.save(payment);
    }

    @Override
    public Optional<Payment> getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepo.findByOrder_Id(orderId).orElse(null);
        return Optional.ofNullable(payment);
    }

    @Override
    public Optional<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentRepo.findAll();
        return Optional.ofNullable(payments);
    }
}
