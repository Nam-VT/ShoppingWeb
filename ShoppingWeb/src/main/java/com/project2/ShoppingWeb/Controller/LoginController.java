package com.project2.ShoppingWeb.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.project2.ShoppingWeb.Service.UserService;
import com.project2.ShoppingWeb.DTO.LoginRequest;
import com.project2.ShoppingWeb.Entity.User;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    @PostMapping("/register")
    public User registerUser(@RequestBody User registrationRequest) {
        return userService.registerUser(registrationRequest);
    }

    @PostMapping("/login")
    public LoginRequest loginUser(@RequestBody LoginRequest loginRequest) {
        return userService.loginUser(loginRequest);
    }
}
