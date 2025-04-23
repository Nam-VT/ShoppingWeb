import axios from "axios";

export default class ApiService {

    static BASE_URL = "http://localhost:8080";

    static getHeader() {
        const token = localStorage.getItem("token");
        console.log('Current token:', token); // Debug log
        
        if (!token) {
            console.warn('No token found in localStorage');
            return {
                "Content-Type": "application/json"
            };
        }

        return {
            'Authorization': `Bearer ${token}`,
            "Content-Type": "application/json"
        };
    }

    /**AUTH && USERS API */
    static async registerUser(registration) {
        const response = await axios.post(`${this.BASE_URL}/auth/register`, registration);
        return response.data;
    }

    static async loginUser(loginDetails) {
        const response = await axios.post(`${this.BASE_URL}/auth/login`, loginDetails);
        return response.data;
    }

    static async getLoggedInUserInfo() {
        try {
            const response = await axios.get(`${this.BASE_URL}/user/info`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            console.error("Error fetching user info:", error);
            return null;
        }
    }

    static async getUserById(userId) {
        const response = await axios.get(`${this.BASE_URL}/user/get-by-id`, {
            headers: this.getHeader(),
            params: { id: userId }
        });
        return response.data;
    }

    static async getAllUsers() {
        const response = await axios.get(`${this.BASE_URL}/user/get-all`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    /**PRODUCT ENDPOINT */
    static async addProduct(formData) {
        const token = localStorage.getItem("token");
        const response = await axios.post(`${this.BASE_URL}/product/create`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
                'Authorization': token ? `Bearer ${token}` : undefined
            }
        });
        return response.data;
    }

    static async updateProduct(formData) {
        try {
            const token = localStorage.getItem("token");
            
            // Log request details
            console.log('=== DEBUG: Request Details ===');
            console.log('URL:', `${this.BASE_URL}/product/update`);
            console.log('Headers:', {
                'Content-Type': 'multipart/form-data',
                'Authorization': `Bearer ${token}`
            });
            console.log('FormData content:');
            for (let [key, value] of formData.entries()) {
                if (key === 'image') {
                    console.log('image:', value ? `File: ${value.name}` : 'No image');
                } else {
                    console.log(`${key}:`, value, `(type: ${typeof value})`);
                }
            }

            const response = await axios.put(
                `${this.BASE_URL}/product/update`,
                formData,
                {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                        'Authorization': `Bearer ${token}`
                    }
                }
            );

            console.log('=== DEBUG: Response Success ===');
            console.log('Status:', response.status);
            console.log('Data:', response.data);

            return response.data;
        } catch (error) {
            console.error('=== DEBUG: Error Details ===');
            console.error('Request Config:', {
                url: error.config?.url,
                method: error.config?.method,
                headers: error.config?.headers
            });
            console.error('Response Error:', {
                status: error.response?.status,
                statusText: error.response?.statusText,
                data: error.response?.data
            });
            throw error;
        }
    }

    static async getAllProducts() {
        const response = await axios.get(`${this.BASE_URL}/product/get-all-product`);
        return response.data;
    }

    static async searchProducts(searchValue) {
        const response = await axios.get(`${this.BASE_URL}/product/search`, {
            params: { searchValue }
        });
        return response.data;
    }

    static async getAllProductsByCategoryId(categoryId) {
        const response = await axios.get(`${this.BASE_URL}/product/get-product-by-category`, {
            params: { categoryId }
        });
        return response.data;
    }

    static async getProductById(productId) {
        const response = await axios.get(`${this.BASE_URL}/product/get-product-by-id`, {
            params: { id: productId }
        });
        return response.data;
    }

    static async deleteProduct(productId) {
        const response = await axios.delete(`${this.BASE_URL}/product/delete/${productId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    /**CATEGORY */
    static async createCategory(category) {
        const response = await axios.post(`${this.BASE_URL}/category/create`, category, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async getAllCategory() {
        const response = await axios.get(`${this.BASE_URL}/category/get-all-category`);
        return response.data;
    }

    static async getCategoryById(categoryId) {
        const response = await axios.get(`${this.BASE_URL}/category/get-category-by-id`, {
            params: { categoryId }
        });
        return response.data;
    }

    static async updateCategory(categoryId, name) {
        const response = await axios.put(`${this.BASE_URL}/category/update`, null, {
            headers: this.getHeader(),
            params: { categoryId, name }
        });
        return response.data;
    }

    static async deleteCategory(categoryId) {
        const response = await axios.delete(`${this.BASE_URL}/category/delete`, {
            headers: this.getHeader(),
            params: { categoryId }
        });
        return response.data;
    }

    /**ADDRESS */
    static async saveAddress(address) {
        try {
            console.log("Saving address:", address);
            
            // Đảm bảo chỉ gửi các trường cần thiết
            const addressDto = {
                id: address.id || null,
                street: address.street || '',
                city: address.city || '',
                state: address.state || '',
                country: address.country || ''
            };
            
            const response = await axios.post(`${this.BASE_URL}/address/save`, addressDto, {
                headers: this.getHeader(),
                withCredentials: true
            });
            
            console.log("Address saved successfully:", response.data);
            return response.data;
        } catch (error) {
            console.error("Error saving address:", error);
            if (error.response) {
                console.error("Error response:", error.response.data);
            }
            throw error;
        }
    }

    /**CHAT BOT */
    static async chatWithBot(prompt) {
        try {
            const response = await axios.get(`${this.BASE_URL}/bot/chat`, {
                params: { prompt }
            });
            return response.data;
        } catch (error) {
            console.error('Lỗi khi chat với bot:', error);
            throw error;
        }
    }

    /**AUTHENTICATION CHECKER */
    static logout() {
        localStorage.removeItem('token');
        localStorage.removeItem('role');
    }

    static isAuthenticated() {
        const token = localStorage.getItem('token');
        return !!token;
    }

    static isAdmin() {
        const role = localStorage.getItem('role');
        return role === 'ADMIN';
    }

    static setupAxiosInterceptors() {
        axios.interceptors.response.use(
            (response) => response,
            async (error) => {
                const originalRequest = error.config;
                
                // Nếu là lỗi 401 và chưa thử refresh token
                if (error.response.status === 401 && !originalRequest._retry) {
                    originalRequest._retry = true;
                    
                    try {
                        // Thử refresh token
                        await ApiService.refreshToken();
                        
                        // Cập nhật token mới vào header
                        originalRequest.headers['Authorization'] = 
                            'Bearer ' + localStorage.getItem('token');
                            
                        // Thử lại request ban đầu
                        return axios(originalRequest);
                    } catch (refreshError) {
                        // Nếu refresh token thất bại
                        ApiService.logout();
                        window.location.href = '/login';
                        return Promise.reject(refreshError);
                    }
                }
                return Promise.reject(error);
            }
        );
    }

    static async refreshToken() {
        try {
            const refreshToken = localStorage.getItem('refreshToken');
            const response = await axios.post(`${this.BASE_URL}/auth/refresh-token`, {
                refreshToken
            });
            
            if (response.data.token) {
                localStorage.setItem('token', response.data.token);
                return response.data.token;
            }
            throw new Error('No token received');
        } catch (error) {
            throw error;
        }
    }

    /**REVIEW ENDPOINTS */
    static async getProductReviews(productId) {
        const response = await axios.get(`${this.BASE_URL}/reviews/product/${productId}`);
        return response.data;
    }

    static async createReview(review) {
        const response = await axios.post(`${this.BASE_URL}/reviews`, review, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async updateReview(reviewId, review) {
        const response = await axios.put(`${this.BASE_URL}/reviews/${reviewId}`, review, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async deleteReview(reviewId) {
        const response = await axios.delete(`${this.BASE_URL}/reviews/${reviewId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    /**ORDER ENDPOINTS */
    static async createOrder(orderData) {
        try {
            console.log("Creating order:", orderData);
            
            // Kiểm tra xem dữ liệu có đúng định dạng không
            const orderItems = orderData.orderItems || orderData.items;
            
            if (!orderItems || !Array.isArray(orderItems)) {
                throw new Error("Dữ liệu đơn hàng không hợp lệ: Thiếu danh sách sản phẩm");
            }
            
            // Đảm bảo dữ liệu gửi đi phù hợp với OrderDTO từ backend
            const orderDTO = {
                totalPrice: orderData.totalPrice,
                paymentMethod: orderData.paymentMethod,
                shippingAddress: orderData.shippingAddress,
                status: orderData.status,
                orderItems: orderItems.map(item => ({
                    productId: item.productId,
                    quantity: item.quantity,
                    price: item.price
                }))
            };
            
            const response = await axios.post(`${this.BASE_URL}/order/create`, orderDTO, {
                headers: this.getHeader(),
                withCredentials: true
            });
            
            console.log("Order creation response:", response);
            return response.data;
        } catch (error) {
            console.error("Order creation error:", error);
            if (error.response) {
                console.error("Error response data:", error.response.data);
            }
            throw error;
        }
    }

    static async createVNPayUrl(orderId, amount) {
        try {
            const response = await axios.post(`${this.BASE_URL}/payment/create`, null, {
                headers: this.getHeader(),
                params: { orderId, amount }
            });
            return response.data;
        } catch (error) {
            console.error("VNPAY error:", error);
            throw error;
        }
    }

    static async createZaloPayUrl(orderId, amount) {
        try {
            const response = await axios.post(`${this.BASE_URL}/zalopay/create-order`, {
                amount: Math.round(amount),  // ZaloPay yêu cầu số nguyên
                orderId: orderId  // Thêm orderId vào request
            }, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            console.error("ZaloPay error:", error.response || error);
            throw error;
        }
    }

    static async getOrderStatus(orderId) {
        const token = this.getToken();
        console.log("Checking order status for ID:", orderId);
        try {
            const response = await axios.get(`${this.BASE_URL}/api/orders/${orderId}/status`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            console.log("Order status response:", response.data);
            return response.data;
        } catch (error) {
            console.error("Error checking order status:", error);
            throw error;
        }
    }
}

ApiService.setupAxiosInterceptors();