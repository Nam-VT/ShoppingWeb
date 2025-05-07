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
import org.springframework.web.bind.annotation.CrossOrigin;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {

    private final UserService userService;

    @GetMapping("/get-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers() {
        try {
            log.info("Fetching all users");
            List<User> users = userService.getAllUsers();
            log.info("Found {} users", users.size());
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error fetching users: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error fetching users: " + e.getMessage());
        }
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
