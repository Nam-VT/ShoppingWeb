package com.project2.ShoppingWeb.DTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchCriteria {
    private String name;            // từ khóa tên sản phẩm
    private String description;     // từ khóa trong mô tả
    private PriceRange priceRange; // khoảng giá
    private List<String> categories; // danh mục sản phẩm
    private Boolean availability;   // còn hàng hay không
    private String sortBy;         // sắp xếp theo tiêu chí nào
    private List<String> keywords; // các từ khóa khác
    private Boolean valid;
}