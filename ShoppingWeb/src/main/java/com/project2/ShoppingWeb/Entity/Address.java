package com.project2.ShoppingWeb.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Data
@Entity
@Table(name = "address")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String street;
    private String city;
    private String state;
    private String country;
    
    @JsonBackReference(value = "user-address")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private  LocalDateTime createdAt;

    public String getFullAddress() {
        return street + ", " + city + ", " + state + ", " + country;
    }
}
