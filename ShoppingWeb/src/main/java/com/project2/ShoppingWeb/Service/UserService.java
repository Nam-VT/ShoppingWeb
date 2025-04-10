package com.project2.ShoppingWeb.Service;

import java.util.List;
import com.project2.ShoppingWeb.DTO.LoginRequest;
import com.project2.ShoppingWeb.DTO.LoginResponse; 
import com.project2.ShoppingWeb.Entity.User;
import com.project2.ShoppingWeb.DTO.ChangePasswordRequest;
public interface UserService {
    User registerUser(User registrationRequest);
    LoginResponse loginUser(LoginRequest loginRequest);
    List<User> getAllUsers();
    User getLoginUser();
    User getUserInfoAndOrderHistory();
    User getUserById(Integer id);
    void changePassword(String email, ChangePasswordRequest request);
}
