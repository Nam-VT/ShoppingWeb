import React, { createContext, useState, useContext, useEffect } from 'react';

// Tạo context
export const AppContext = createContext();

// Custom hook để sử dụng context
export const useAppContext = () => useContext(AppContext);

// Provider component
export const AppProvider = ({ children }) => {
    // State cho đơn hàng
    const [currentOrder, setCurrentOrder] = useState(null);
    const [pendingOrder, setPendingOrder] = useState(null);
    const [orderId, setOrderId] = useState(null);

    // Hàm cập nhật đơn hàng hiện tại
    const updateCurrentOrder = (order) => {
        setCurrentOrder(order);
    };

    // Hàm cập nhật đơn hàng đang chờ thanh toán
    const updatePendingOrder = (order) => {
        setPendingOrder(order);
        
        // Lưu vào localStorage để theo dõi
        if (order) {
            localStorage.setItem('pendingOrderId', order.id);
            localStorage.setItem('pendingOrderTime', new Date().toISOString());
        } else {
            localStorage.removeItem('pendingOrderId');
            localStorage.removeItem('pendingOrderTime');
        }
    };

    // Thêm UseEffect để kiểm tra cách polling hoạt động
    useEffect(() => {
        // Log để debug
        console.log("Current order ID:", orderId);
        console.log("Local storage pendingOrderId:", localStorage.getItem('pendingOrderId'));
    }, [orderId]);

    // Cung cấp context value
    const value = {
        currentOrder,
        pendingOrder,
        updateCurrentOrder,
        updatePendingOrder
    };

    return (
        <AppContext.Provider value={value}>
            {children}
        </AppContext.Provider>
    );
}; 