package com.project2.ShoppingWeb.DTO;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiError {
    private int status;
    private String message;
    private String path;
    private LocalDateTime timestamp;
}
