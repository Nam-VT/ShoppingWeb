import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import '../../style/adminOrderPage.css'
import Pagination from "../common/Pagination";
import ApiService from "../../service/ApiService";

const OrderStatus = ["PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED", "RETURNED"];
const PaymentStatus = ["PENDING", "PAID", "FAILED", "REFUNDED"];

const AdminOrdersPage = () => {
    const [orders, setOrders] = useState([]);
    const [searchStatus, setSearchStatus] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(0);
    const [error, setError] = useState(null);
    const [isLoading, setIsLoading] = useState(false);
    const itemsPerPage = 10;
    const navigate = useNavigate();

    useEffect(() => {
        if (!ApiService.isAdmin()) {
            setError('Bạn không có quyền truy cập trang này');
            return;
        }
        fetchOrders();
    }, [searchStatus, currentPage]);

    const fetchOrders = async () => {
        try {
            setIsLoading(true);
            setError(null);
            let response;
            if (searchStatus) {
                response = await ApiService.getOrdersByStatus(searchStatus);
            } else {
                response = await ApiService.getAllOrders();
            }
            let orderList = [];
            if (Array.isArray(response)) {
                orderList = response;
            } else if (response && Array.isArray(response.content)) {
                orderList = response.content;
            } else if (response && Array.isArray(response.orders)) {
                orderList = response.orders;
            } else {
                console.warn("Unexpected response format:", response);
                orderList = [];
            }
            setOrders(orderList);
            setTotalPages(Math.ceil(orderList.length / itemsPerPage));
        } catch (error) {
            console.error("Error fetching orders:", error);
            setError(error.response?.data?.message || error.message || 'Không thể tải danh sách đơn hàng');
        } finally {
            setIsLoading(false);
        }
    };

    const handleOrderDetails = (id) => {
        navigate(`/admin/order-details/${id}`)
    }

    const handleDeleteOrder = async (orderId) => {
        if (window.confirm('Bạn có chắc chắn muốn xóa đơn hàng này?')) {
            try {
                await ApiService.deleteOrder(orderId);
                setOrders(orders.filter(order => order.id !== orderId));
                setError('Xóa đơn hàng thành công');
                setTimeout(() => {
                    setError('');
                }, 3000);
            } catch (error) {
                setError(error.response?.data?.message || 'Không thể xóa đơn hàng');
                setTimeout(() => {
                    setError('');
                }, 3000);
            }
        }
    }

    // Thay đổi trạng thái thanh toán trực tiếp
    const handleUpdatePaymentStatus = async (orderId, status) => {
        try {
            await ApiService.updatePaymentStatus(orderId, status);
            fetchOrders(); // Refresh danh sách
            setError('Cập nhật trạng thái thanh toán thành công');
            setTimeout(() => {
                setError('');
            }, 3000);
        } catch (error) {
            setError(error.response?.data?.message || 'Không thể cập nhật trạng thái thanh toán');
            setTimeout(() => {
                setError('');
            }, 3000);
        }
    }

    // Tính toán orders cho trang hiện tại
    const currentOrders = orders.slice(
        (currentPage - 1) * itemsPerPage,
        currentPage * itemsPerPage
    );

    return (
        <div className="admin-orders-page">
            <h2>Quản lý đơn hàng</h2>
            {error && <p className="error-message">{error}</p>}
            {isLoading && <p className="loading-message">Đang tải...</p>}
            
            <div className="search-container">
                <div className="search-group">
                    <label>Lọc theo trạng thái:</label>
                    <select 
                        value={searchStatus} 
                        onChange={e => { setSearchStatus(e.target.value); setCurrentPage(1); }}
                        disabled={isLoading}
                    >
                        <option value="">Tất cả</option>
                        {OrderStatus.map(status => (
                            <option key={status} value={status}>{status}</option>
                        ))}
                    </select>
                </div>
            </div>

            <table className="orders-table">
                <thead>
                    <tr>
                        <th>Mã đơn hàng</th>
                        <th>Khách hàng</th>
                        <th>Trạng thái</th>
                        <th>Trạng thái thanh toán</th>
                        <th>Tổng tiền</th>
                        <th>Ngày đặt</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    {currentOrders.map(order => (
                        <tr key={order.id}>
                            <td>{order.id}</td>
                            <td>{order.customerName || 'N/A'}</td>
                            <td>{order.status}</td>
                            <td>
                                <select 
                                    value={order.paymentStatus || 'PENDING'}
                                    onChange={(e) => handleUpdatePaymentStatus(order.id, e.target.value)}
                                >
                                    {PaymentStatus.map(status => (
                                        <option key={status} value={status}>{status}</option>
                                    ))}
                                </select>
                            </td>
                            <td>${order.totalPrice ? order.totalPrice.toFixed(2) : '0.00'}</td>
                            <td>{order.createdAt ? new Date(order.createdAt).toLocaleDateString() : 'N/A'}</td>
                            <td>
                                <div className="action-buttons">
                                    <button onClick={() => handleOrderDetails(order.id)}>
                                        Chi tiết
                                    </button>
                                    <button 
                                        className="delete-btn"
                                        onClick={() => handleDeleteOrder(order.id)}
                                    >
                                        Xóa
                                    </button>
                                </div>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={(page) => setCurrentPage(page)}
            />
        </div>
    )
}

export default AdminOrdersPage;