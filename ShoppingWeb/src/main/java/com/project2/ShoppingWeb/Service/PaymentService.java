package com.project2.ShoppingWeb.Service;

import com.project2.ShoppingWeb.Entity.Order;
import com.project2.ShoppingWeb.Entity.Payment;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

public interface PaymentService {
    String createPaymentUrl(Long amount, String orderId, String orderInfo);
    Payment savePayment(Order order, BigDecimal amount);
    Optional<Payment> getPaymentByOrderId(Long orderId);
    String hmacSHA512(String key, String data);
    Optional<List<Payment>> getAllPayments();
}