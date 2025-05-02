import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import ApiService from '../../service/ApiService';
import '../../style/orderHistory.css';

function OrderHistoryPage() {
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchOrders();
    }, []);

    const fetchOrders = async () => {
        try {
            setLoading(true);
            const response = await ApiService.getUserOrders();
            console.log("User orders:", response);
            
            // Sắp xếp đơn hàng mới nhất lên đầu
            const sortedOrders = Array.isArray(response) 
                ? [...response].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)) 
                : [];
                
            setOrders(sortedOrders);
            setLoading(false);
        } catch (error) {
            console.error("Error fetching orders:", error);
            setError("Không thể tải thông tin đơn hàng. Vui lòng thử lại sau.");
            setLoading(false);
        }
    };

    const getStatusBadgeClass = (status) => {
        switch (status) {
            case 'PENDING': return 'badge-warning';
            case 'CONFIRMED': return 'badge-primary';
            case 'PROCESSING': return 'badge-info';
            case 'SHIPPED': return 'badge-info';
            case 'DELIVERED': return 'badge-success';
            case 'CANCELLED': return 'badge-danger';
            default: return 'badge-secondary';
        }
    };

    const getPaymentStatusBadgeClass = (status) => {
        switch (status) {
            case 'PAID': return 'badge-success';
            case 'PENDING': return 'badge-warning';
            case 'PROCESSING': return 'badge-info';
            case 'FAILED': return 'badge-danger';
            default: return 'badge-secondary';
        }
    };

    if (loading) {
        return <div className="loading-spinner">Đang tải...</div>;
    }

    if (error) {
        return <div className="error-message">{error}</div>;
    }

    return (
        <div className="order-history-page">
            <h2>Lịch sử đơn hàng</h2>
            
            {orders.length === 0 ? (
                <div className="empty-orders">
                    <p>Bạn chưa có đơn hàng nào.</p>
                    <Link to="/" className="shop-now-btn">Mua sắm ngay</Link>
                </div>
            ) : (
                <div className="orders-list">
                    {orders.map(order => (
                        <div key={order.id} className="order-card">
                            <div className="order-header">
                                <div>
                                    <h3>Đơn hàng #{order.id}</h3>
                                    <p className="order-date">
                                        Ngày đặt: {new Date(order.createdAt).toLocaleDateString()}
                                    </p>
                                </div>
                                <div className="order-status-badges">
                                    <span className={`status-badge ${getStatusBadgeClass(order.status)}`}>
                                        {order.status}
                                    </span>
                                    <span className={`status-badge ${getPaymentStatusBadgeClass(order.paymentStatus)}`}>
                                        {order.paymentStatus}
                                    </span>
                                </div>
                            </div>
                            
                            <div className="order-items">
                                {order.orderItems.map(item => (
                                    <div key={item.id} className="order-item">
                                        <div className="item-image">
                                            <img 
                                                src={item.productImageUrl || '/placeholder-image.jpg'} 
                                                alt={item.productName} 
                                            />
                                        </div>
                                        <div className="item-details">
                                            <h4>{item.productName}</h4>
                                            <p>Số lượng: {item.quantity}</p>
                                            <p>Giá: ${item.price.toFixed(2)}</p>
                                        </div>
                                    </div>
                                ))}
                            </div>
                            
                            <div className="order-footer">
                                <div className="order-total">
                                    <p>Tổng thanh toán: <strong>${order.totalPrice.toFixed(2)}</strong></p>
                                    <p>Phương thức thanh toán: {order.paymentMethod}</p>
                                </div>
                                <Link to={`/order-details/${order.id}`} className="view-details-btn">
                                    Xem chi tiết
                                </Link>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
}

export default OrderHistoryPage; 