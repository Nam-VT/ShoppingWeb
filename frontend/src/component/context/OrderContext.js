import React, {createContext, useState, useContext, useEffect} from "react";

const OrderContext = createContext();

export const OrderProvider = ({children}) => {
    // State cho đơn hàng
    const [currentOrder, setCurrentOrder] = useState(null);
    const [pendingOrder, setPendingOrder] = useState(null);

    // Khôi phục trạng thái từ localStorage khi component mount
    useEffect(() => {
        const pendingOrderId = localStorage.getItem('pendingOrderId');
        const pendingOrderTime = localStorage.getItem('pendingOrderTime');
        
        if (pendingOrderId && pendingOrderTime) {
            setPendingOrder({
                id: pendingOrderId,
                createdAt: pendingOrderTime
            });
        }
    }, []);

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

    return (
        <OrderContext.Provider 
            value={{
                currentOrder, 
                pendingOrder, 
                updateCurrentOrder, 
                updatePendingOrder
            }}
        >
            {children}
        </OrderContext.Provider>
    );
};

export const useOrder = () => useContext(OrderContext); 