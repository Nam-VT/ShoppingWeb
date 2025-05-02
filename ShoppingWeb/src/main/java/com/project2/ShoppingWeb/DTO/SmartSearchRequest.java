package com.project2.ShoppingWeb.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmartSearchRequest {
    private String searchQuery;  // câu query tìm kiếm của người dùng, ví dụ: "tìm điện thoại giá rẻ"
}
