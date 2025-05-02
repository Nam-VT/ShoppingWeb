import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import ApiService from '../../service/ApiService';
import '../../style/orderDetails.css';

function OrderDetailsPage() {
    const { orderId } = useParams();
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchOrderDetails();
    }, [orderId]);

    const fetchOrderDetails = async () => {
        try {
            setLoading(true);
            const response = await ApiService.getOrderById(orderId);
            console.log("Order details:", response);
            
            if (response) {
                setOrder(response);
            } else {
                setError("Không tìm thấy thông tin đơn hàng");
            }
            setLoading(false);
        } catch (error) {
            console.error("Error fetching order details:", error);
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
        return (
            <div className="error-container">
                <div className="error-message">{error}</div>
                <Link to="/orders" className="back-button">Quay lại danh sách đơn hàng</Link>
            </div>
        );
    }

    if (!order) {
        return (
            <div className="error-container">
                <div className="error-message">Không tìm thấy đơn hàng</div>
                <Link to="/orders" className="back-button">Quay lại danh sách đơn hàng</Link>
            </div>
        );
    }

    return (
        <div className="order-details-page">
            <div className="page-header">
                <h2>Chi tiết đơn hàng #{order.id}</h2>
                <Link to="/orders" className="back-button">Quay lại</Link>
            </div>
            
            <div className="order-info-card">
                <div className="order-info-header">
                    <div>
                        <h3>Thông tin đơn hàng</h3>
                        <p>Ngày đặt: {new Date(order.createdAt).toLocaleDateString()}</p>
                        <p>Cập nhật cuối: {new Date(order.updatedAt).toLocaleDateString()}</p>
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
                
                <div className="order-details-grid">
                    <div className="detail-section">
                        <h4>Thông tin giao hàng</h4>
                        <p><strong>Tên:</strong> {order.customerName}</p>
                        <p><strong>Email:</strong> {order.customerEmail}</p>
                        <p><strong>Địa chỉ:</strong> {order.shippingAddress}</p>
                    </div>
                    
                    <div className="detail-section">
                        <h4>Thông tin thanh toán</h4>
                        <p><strong>Phương thức:</strong> {order.paymentMethod}</p>
                        <p><strong>Trạng thái:</strong> {order.paymentStatus}</p>
                        <p><strong>Tổng tiền:</strong> ${order.totalPrice.toFixed(2)}</p>
                    </div>
                </div>
            </div>
            
            <div className="order-items-section">
                <h3>Sản phẩm đặt mua</h3>
                <div className="order-items-list">
                    {order.orderItems.map(item => (
                        <div key={item.id} className="order-item-card">
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
                                <p>Thành tiền: ${(item.price * item.quantity).toFixed(2)}</p>
                            </div>
                            <div className="item-status">
                                <span className={`status-badge ${getStatusBadgeClass(item.status)}`}>
                                    {item.status}
                                </span>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
            
            <div className="order-summary">
                <h3>Tổng thanh toán</h3>
                <div className="summary-table">
                    <div className="summary-row">
                        <span>Tổng tiền hàng:</span>
                        <span>${order.totalPrice.toFixed(2)}</span>
                    </div>
                    <div className="summary-row">
                        <span>Phí vận chuyển:</span>
                        <span>$0.00</span>
                    </div>
                    <div className="summary-row total">
                        <span>Tổng cộng:</span>
                        <span>${order.totalPrice.toFixed(2)}</span>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default OrderDetailsPage; 