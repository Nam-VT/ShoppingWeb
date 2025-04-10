package com.project2.ShoppingWeb.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.project2.ShoppingWeb.Service.UserService;

import jakarta.validation.Valid;

import com.project2.ShoppingWeb.DTO.LoginRequest;
import com.project2.ShoppingWeb.Entity.User;
import com.project2.ShoppingWeb.DTO.LoginResponse;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public User registerUser(@RequestBody User registrationRequest) {
        return userService.registerUser(registrationRequest);
    }


    // public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest loginRequest) {
    //     return ResponseEntity.ok(userService.loginUser(loginRequest));
    // }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.loginUser(request);
            response.setMessage("Đăng nhập thành công");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(LoginResponse.builder()
                    .message(e.getMessage())
                    .build());
        }
    }
    
}
