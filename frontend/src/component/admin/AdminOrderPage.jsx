import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import '../../style/adminOrderPage.css'
import Pagination from "../common/Pagination";
import ApiService from "../../service/ApiService";

const OrderStatus = ["PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED", "RETURNED"];

const AdminOrdersPage = () => {
    const [orders, setOrders] = useState([]);
    const [searchStatus, setSearchStatus] = useState('');
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(0);
    const [error, setError] = useState(null);
    const itemsPerPage = 10;
    const navigate = useNavigate();

    useEffect(() => {
        fetchOrders();
    }, [searchStatus, currentPage]);

    const fetchOrders = async () => {
        try {
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
            setError(error.response?.data?.message || error.message || 'Không thể tải danh sách đơn hàng');
            setTimeout(() => {
                setError('');
            }, 3000);
        }
    };

    const handleSearchStatusChange = async (e) => {
        setSearchStatus(e.target.value);
        setCurrentPage(1);
    }

    const handleOrderDetails = (id) => {
        navigate(`/admin/order-details/${id}`)
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
            
            <div className="search-container">
                <label>Lọc theo trạng thái:</label>
                <select value={searchStatus} onChange={handleSearchStatusChange}>
                    <option value="">Tất cả</option>
                    {OrderStatus.map(status => (
                        <option key={status} value={status}>{status}</option>
                    ))}
                </select>
            </div>

            <table className="orders-table">
                <thead>
                    <tr>
                        <th>Mã đơn hàng</th>
                        <th>Khách hàng</th>
                        <th>Trạng thái</th>
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
                            <td>${order.totalPrice ? order.totalPrice.toFixed(2) : '0.00'}</td>
                            <td>{order.createdAt ? new Date(order.createdAt).toLocaleDateString() : 'N/A'}</td>
                            <td>
                                <button onClick={() => handleOrderDetails(order.id)}>
                                    Chi tiết
                                </button>
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