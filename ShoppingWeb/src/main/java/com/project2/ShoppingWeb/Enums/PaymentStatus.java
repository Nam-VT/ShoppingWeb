package com.project2.ShoppingWeb.Enums;

public enum PaymentStatus {
    PENDING, // Chờ xác nhận
    PROCESSING, // Đang xử lý
    PAID, // Đã thanh toán
    CONFIRMED, // Đã xác nhận
    FAILED, // Thất bại
    CANCELLED // Đã hủy
}