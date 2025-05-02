import React, { useState, useEffect } from "react";
import { useParams } from "react-router-dom";
import '../../style/adminOrderDetails.css'
import ApiService from "../../service/ApiService";
import axios from "axios";


const OrderStatus = ["PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED", "RETURNED"];

const AdminOrderDetailsPage = () => {
    const { itemId } = useParams();
    const [order, setOrder] = useState(null);
    const [message, setMessage] = useState('');
    const [selectedStatus, setSelectedStatus] = useState('');

    useEffect(() => {
        fetchOrderDetails(itemId);
    }, [itemId]);

    const fetchOrderDetails = async (orderId) => {
        try {
            const response = await ApiService.getOrderById(orderId);
            if (response) {
                setOrder(response);
                setSelectedStatus(response.status);
            }
        } catch (error) {
            setMessage(error.response?.data?.message || "Không thể tải thông tin đơn hàng");
        }
    }

    const handleStatusChange = (newStatus) => {
        setSelectedStatus(newStatus);
    }

    const handleSubmitStatusChange = async () => {
        try {
            await ApiService.updateOrderStatus(itemId, selectedStatus);
            setMessage('Cập nhật trạng thái đơn hàng thành công');
            fetchOrderDetails(itemId);
            
            setTimeout(() => {
                setMessage('');
            }, 3000);
        } catch (error) {
            setMessage(error.response?.data?.message || 'Không thể cập nhật trạng thái đơn hàng');
        }
    }

    if (!order) {
        return <div>Đang tải thông tin đơn hàng...</div>;
    }

    return (
        <div className="order-details-page">
            {message && <div className="message">{message}</div>}
            <h2>Chi tiết đơn hàng #{itemId}</h2>
            
            <div className="order-info">
                <h3>Thông tin đơn hàng</h3>
                <p><strong>Ngày đặt:</strong> {new Date(order.createdAt).toLocaleDateString()}</p>
                <p><strong>Tổng tiền:</strong> ${order.totalPrice}</p>
                <p><strong>Trạng thái:</strong> {order.status}</p>
                <p><strong>Phương thức thanh toán:</strong> {order.paymentMethod}</p>
            </div>

            <div className="customer-info">
                <h3>Thông tin khách hàng</h3>
                <p><strong>Tên:</strong> {order.customerName}</p>
                <p><strong>Email:</strong> {order.customerEmail}</p>
                <p><strong>Địa chỉ:</strong> {order.shippingAddress}</p>
            </div>

            <div className="order-items">
                <h3>Sản phẩm</h3>
                <table>
                    <thead>
                        <tr>
                            <th>Sản phẩm</th>
                            <th>Số lượng</th>
                            <th>Giá</th>
                            <th>Tổng</th>
                        </tr>
                    </thead>
                    <tbody>
                        {order.orderItems.map(item => (
                            <tr key={item.id}>
                                <td>{item.product.name}</td>
                                <td>{item.quantity}</td>
                                <td>${item.price}</td>
                                <td>${item.price * item.quantity}</td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>

            <div className="status-change">
                <h3>Cập nhật trạng thái</h3>
                <select
                    value={selectedStatus}
                    onChange={(e) => handleStatusChange(e.target.value)}
                >
                    {OrderStatus.map(status => (
                        <option key={status} value={status}>{status}</option>
                    ))}
                </select>
                <button onClick={handleSubmitStatusChange}>
                    Cập nhật trạng thái
                </button>
            </div>
        </div>
    );
}

export default AdminOrderDetailsPage;