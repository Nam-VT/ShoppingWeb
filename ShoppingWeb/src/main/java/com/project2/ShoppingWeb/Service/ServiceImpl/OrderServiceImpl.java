package com.project2.ShoppingWeb.Service.ServiceImpl;

import org.springframework.stereotype.Service;
import com.project2.ShoppingWeb.Service.OrderService;
import com.project2.ShoppingWeb.Entity.Order;
import com.project2.ShoppingWeb.Repository.OrderRepo;


@Service
public class OrderServiceImpl implements OrderService {

    OrderRepo orderRepo;

    @Override
    public Order getOrderById(Long id) {
        return orderRepo.findById(id).orElse(null);
    }
}
