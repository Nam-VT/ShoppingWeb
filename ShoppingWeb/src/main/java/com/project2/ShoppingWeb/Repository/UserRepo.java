package com.project2.ShoppingWeb.Repository;

import com.project2.ShoppingWeb.Entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Integer> {

    Optional<User> findByName(String username);
}
