package com.project2.ShoppingWeb.Entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name; 

    @Column(length = 255)
    private String description; 

    @JsonBackReference 
    @ManyToMany(mappedBy = "categories", cascade = CascadeType.ALL)
    private List<Product> products; 

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
    @Builder.Default
    private boolean isActive = true; // Trạng thái danh mục (true = hoạt động, false = ẩn)
}
