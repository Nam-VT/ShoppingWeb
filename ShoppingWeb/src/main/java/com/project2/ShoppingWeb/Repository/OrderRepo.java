package com.project2.ShoppingWeb.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project2.ShoppingWeb.Entity.Order;

public interface OrderRepo extends JpaRepository<Order, Long> {

}
