import React, { useState, useEffect } from "react";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import ApiService from "./ApiService";

export const ProtectedRoute = ({element: Component}) => {
    const location = useLocation();
    const [isChecking, setIsChecking] = useState(true);
    const [isAuthenticated, setIsAuthenticated] = useState(false);

    useEffect(() => {
        const checkAuth = async () => {
            try {
                const token = localStorage.getItem('token');
                console.log('Checking authentication...');
                console.log('Token exists:', !!token);
                
                if (!token) {
                    throw new Error('No token found');
                }

                // Kiểm tra token có hợp lệ không bằng cách gọi API info
                const userInfo = await ApiService.getLoggedInUserInfo();
                console.log('Auth check successful:', userInfo);
                setIsAuthenticated(true);
            } catch (error) {
                console.error('Auth check failed:', error);
                // Xóa token nếu không hợp lệ
                ApiService.logout();
                setIsAuthenticated(false);
            } finally {
                setIsChecking(false);
            }
        };

        checkAuth();
    }, [location]);

    if (isChecking) {
        return <div>Verifying authentication...</div>;
    }

    if (!isAuthenticated) {
        console.log('Not authenticated, redirecting to login');
        return <Navigate to="/login" replace state={{from: location}}/>;
    }

    return Component;
};

export const AdminRoute = ({element: Component}) => {
    const navigate = useNavigate();
    const [isAuthorized, setIsAuthorized] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const verifyAdmin = async () => {
            try {
                // 1. Kiểm tra token tồn tại
                const token = localStorage.getItem('token');
                if (!token) {
                    throw new Error('Chưa đăng nhập');
                }

                // 2. Kiểm tra role là ADMIN
                const role = localStorage.getItem('role');
                if (role !== 'ADMIN') {
                    throw new Error('Không có quyền admin');
                }

                // 3. Xác thực với server
                await ApiService.getLoggedInUserInfo();
                setIsAuthorized(true);
            } catch (error) {
                console.error('Lỗi xác thực admin:', error);
                navigate('/login');
            } finally {
                setIsLoading(false);
            }
        };

        verifyAdmin();
    }, [navigate]);

    if (isLoading) {
        return <div>Đang kiểm tra quyền truy cập...</div>;
    }

    return isAuthorized ? Component : null;
};
