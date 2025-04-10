package com.project2.ShoppingWeb.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import com.project2.ShoppingWeb.Enums.UserRole;

@Data
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Name is required")
    @Column(unique = true)
    private String name;

    @NotBlank(message = "Password is required")
    private String password;

    @Column(unique = true)
    @NotBlank(message = "Email is required")
    private String email;

    private UserRole role;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "user")
    private Address address;


    private String phoneNumber;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)   
    private List<OrderItem> orderItemlist;

    private final LocalDateTime created_at = LocalDateTime.now();
}
