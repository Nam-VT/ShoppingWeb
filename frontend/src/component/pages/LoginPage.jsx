import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import ApiService from "../../service/ApiService";
import '../../style/register.css'


const LoginPage = () => {

    const [formData, setFormData] = useState({
        email: '',
        password: ''
    });

    const [message, setMessage] = useState(null);
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);


    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        try {
            console.log('Attempting login with:', formData.email);
            const response = await ApiService.loginUser(formData);
            console.log('Login response:', response);

            // Kiểm tra response có đầy đủ thông tin không
            if (!response.token || !response.role) {
                throw new Error('Invalid response from server');
            }

            // Lưu thông tin vào localStorage
            localStorage.setItem('token', response.token);
            localStorage.setItem('role', response.role);
            console.log('Stored token and role in localStorage');

            setMessage("Login successful!");

            // Điều hướng dựa vào role
            setTimeout(() => {
                if (response.role === 'ADMIN') {
                    console.log('Redirecting to admin dashboard...');
                    navigate("/admin");
                } else {
                    console.log('Redirecting to profile...');
                    navigate("/profile");
                }
            }, 1000);

        } catch (error) {
            console.error('Login error:', error);
            setMessage(error.response?.data?.message || 
                      error.message || 
                      "Unable to login. Please try again.");
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <div className="register-page">
            <h2>Login</h2>
            {message && (
                <p className={`message ${message.includes('successful') ? 'success' : 'error'}`}>
                    {message}
                </p>
            )}
            <form onSubmit={handleSubmit}>
                <label>Email: </label>
                <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    required
                    disabled={isLoading}
                />
                    
                <label>Password: </label>
                <input
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    required
                    disabled={isLoading}
                />

                <button type="submit" disabled={isLoading}>
                    {isLoading ? "Logging in..." : "Login"}
                </button>
                    
                <p className="register-link">
                    Don't have an account? <a href="/register">Register</a>
                </p>
            </form>
        </div>
    )
}

export default LoginPage;