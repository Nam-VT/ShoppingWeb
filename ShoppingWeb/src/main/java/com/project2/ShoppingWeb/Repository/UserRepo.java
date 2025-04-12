package com.project2.ShoppingWeb.Repository;

import com.project2.ShoppingWeb.Entity.User;
import com.project2.ShoppingWeb.Enums.UserRole;
// import com.project2.ShoppingWeb.DTO.ChangePasswordRequest;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, Integer> {

    Optional<User> findByName(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByRole(UserRole role);

    boolean existsByEmail(String email);

}
