package com.project2.ShoppingWeb.DTO;

import java.util.List;

import com.project2.ShoppingWeb.Entity.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmartSearchResponse {
    private List<Product> products;    // danh sách sản phẩm tìm được
    private String explanation;        // giải thích kết quả tìm kiếm
    private SearchCriteria criteria;   // tiêu chí tìm kiếm đã được AI phân tích
}
