package com.project2.ShoppingWeb.Service;

import java.util.List;

import com.project2.ShoppingWeb.DTO.LoginRequest;

import com.project2.ShoppingWeb.Entity.User;

public interface UserService {
    User registerUser(User registrationRequest);
    LoginRequest loginUser(LoginRequest loginRequest);
    List<User> getAllUsers();
    User getLoginUser();
    User getUserInfoAndOrderHistory();
    User getUserById(Integer id);
}
