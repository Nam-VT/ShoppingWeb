package com.project2.ShoppingWeb.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project2.ShoppingWeb.Entity.User;
import com.project2.ShoppingWeb.Service.UserService;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/get-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/info")
    public ResponseEntity<User> getUserInfoAndOrderHistory() {
        return ResponseEntity.ok(userService.getUserInfoAndOrderHistory());
    }

    @GetMapping("/get-by-id")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@RequestParam Integer id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}
