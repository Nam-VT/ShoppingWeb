import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import ApiService from "../../service/ApiService";
import '../../style/profile.css';
import Pagination from "../common/Pagination";

const ProfilePage = () => {
    const [userInfo, setUserInfo] = useState(null);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const [currentPage, setCurrentPage] = useState(1);
    const itemsPerPage = 5;
    const navigate = useNavigate();

    useEffect(() => {
        // Kiểm tra authentication trước
        if (!ApiService.isAuthenticated()) {
            navigate('/login');
            return;
        }
        fetchUserInfo();
    }, [navigate]);

    const fetchUserInfo = async () => {
        try {
            // Thử lấy thông tin user
            const response = await ApiService.getLoggedInUserInfo();
            console.log("User info response:", response);
            setUserInfo(response);
        } catch (error) {
            console.error("Error fetching user info:", error);
            // Nếu token không hợp lệ hoặc hết hạn
            if (error.response?.status === 401 || error.response?.status === 403) {
                ApiService.logout();
                navigate('/login');
            } else {
                setError(error.response?.data?.message || "Could not fetch user information");
            }
        } finally {
            setLoading(false);
        }
    };

    const handleAddressClick = () => {
        navigate(userInfo.address ? '/edit-address' : '/add-address');
    };

    if (loading) {
        return <div className="loading">Loading...</div>;
    }

    if (error) {
        return <div className="error-message">{error}</div>;
    }

    if (!userInfo) {
        return <div className="no-data">No user information available</div>;
    }

    // Lấy danh sách order, nếu không có thì trả về mảng rỗng
    const orderItemList = userInfo.orderItemlist || [];
    const totalPages = Math.ceil(orderItemList.length / itemsPerPage);
    const paginatedOrders = orderItemList.slice(
        (currentPage - 1) * itemsPerPage,
        currentPage * itemsPerPage
    );

    return (
        <div className="profile-page">
            <h2>Welcome {userInfo.name}</h2>

            <div className="user-info">
                <p><strong>Name: </strong>{userInfo.name}</p>
                <p><strong>Email: </strong>{userInfo.email}</p>
                <p><strong>Phone Number: </strong>{userInfo.phoneNumber || 'Not provided'}</p>
            </div>

            {/* Chỉ hiển thị phần address nếu có */}
            {userInfo.address && (
                <div className="address-section">
                    <h3>Address</h3>
                    <div className="address-info">
                        <p><strong>Street: </strong>{userInfo.address.street}</p>
                        <p><strong>City: </strong>{userInfo.address.city}</p>
                        <p><strong>State: </strong>{userInfo.address.state}</p>
                        <p><strong>Country: </strong>{userInfo.address.country}</p>
                    </div>
                </div>
            )}

            {/* Chỉ hiển thị phần orders nếu có */}
            {userInfo.orderItemlist && userInfo.orderItemlist.length > 0 && (
                <div className="order-section">
                    <h3>Order History</h3>
                    <ul className="order-list">
                        {paginatedOrders.map(order => (
                            <li key={order.id} className="order-item">
                                {order.product && (
                                    <div className="product-info">
                                        <h4>{order.product.name}</h4>
                                        <p><strong>Status: </strong>{order.status}</p>
                                        <p><strong>Quantity: </strong>{order.quantity}</p>
                                        <p><strong>Price: </strong>${order.price.toFixed(2)}</p>
                                    </div>
                                )}
                            </li>
                        ))}
                    </ul>
                    {totalPages > 1 && (
                        <Pagination
                            currentPage={currentPage}
                            totalPages={totalPages}
                            onPageChange={setCurrentPage}
                        />
                    )}
                </div>
            )}
        </div>
    );
};

export default ProfilePage;