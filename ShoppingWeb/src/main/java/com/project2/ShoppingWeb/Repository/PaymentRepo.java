package com.project2.ShoppingWeb.Repository;

import com.project2.ShoppingWeb.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrder_Id(Long orderId);
    
}