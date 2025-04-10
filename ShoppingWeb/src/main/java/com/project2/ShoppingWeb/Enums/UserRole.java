package com.project2.ShoppingWeb.Enums;

public enum UserRole {
    ADMIN(1),
    USER(0);
    
    private final int value;
    
    UserRole(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
}
