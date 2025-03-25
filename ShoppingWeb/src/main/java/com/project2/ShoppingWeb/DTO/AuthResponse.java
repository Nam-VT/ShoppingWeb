package com.project2.ShoppingWeb.DTO;

import com.project2.ShoppingWeb.Entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String message;
    private TokenDTO token;
    private User user;
} 