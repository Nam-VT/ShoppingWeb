package com.project2.ShoppingWeb.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project2.ShoppingWeb.Entity.OrderItem;

public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {

}
