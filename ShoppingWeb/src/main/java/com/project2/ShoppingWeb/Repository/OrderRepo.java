package com.project2.ShoppingWeb.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project2.ShoppingWeb.Entity.Order;
import com.project2.ShoppingWeb.Enums.OrderStatus;

public interface OrderRepo extends JpaRepository<Order, Long> {

    List<Order> findByUserId(long id);

    Optional<Order> findByTransactionId(String transactionId);

    List<Order> findByStatus(String status);

    List<Order> findByStatus(OrderStatus orderStatus);

}
