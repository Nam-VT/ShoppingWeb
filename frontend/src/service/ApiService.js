import axios from "axios";

export default class ApiService {

    static BASE_URL = "http://localhost:8080";

    constructor() {
        this.axiosInstance = axios.create({
            baseURL: 'http://localhost:8080',
            timeout: 10000,
            withCredentials: true,
            headers: {
                'Content-Type': 'application/json',
            }
        });
        
        // Thêm interceptor để xử lý token
        this.axiosInstance.interceptors.request.use(
            (config) => {
                const token = this.getToken();
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }
                return config;
            },
            (error) => Promise.reject(error)
        );
        
        // Thêm interceptor để xử lý lỗi
        this.axiosInstance.interceptors.response.use(
            (response) => response,
            (error) => {
                // Xử lý lỗi CORS hoặc lỗi mạng
                if (error.code === 'ERR_NETWORK' || !error.response) {
                    console.error('Network error or CORS issue:', error.message);
                    return Promise.reject(new Error('Lỗi kết nối đến server. Vui lòng thử lại sau.'));
                }
                return Promise.reject(error);
            }
        );
    }

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
        try {
            const response = await axios.post(`${this.BASE_URL}/reviews`, review, {
                headers: this.getHeader(),
                withCredentials: true
            });
            return response.data;
        } catch (error) {
            console.error("Error creating review:", error);
            throw error;
        }
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
            
            // Kiểm tra cấu trúc của từng item
            orderItems.forEach(item => {
                if (!item.productId) {
                    console.error("Item missing productId:", item);
                    throw new Error("Thiếu productId trong item");
                }
                if (!item.quantity || item.quantity <= 0) {
                    console.error("Item with invalid quantity:", item);
                    throw new Error("Số lượng sản phẩm phải lớn hơn 0");
                }
                if (!item.price || item.price <= 0) {
                    console.error("Item with invalid price:", item);
                    throw new Error("Giá sản phẩm phải lớn hơn 0");
                }
            });
            
            // Đảm bảo dữ liệu gửi đi phù hợp với OrderDTO từ backend
            const orderDTO = {
                totalPrice: orderData.totalPrice,
                paymentMethod: orderData.paymentMethod,
                shippingAddress: orderData.shippingAddress,
                status: orderData.status,
                orderItems: orderItems.map(item => ({
                    productId: item.productId,
                    quantity: item.quantity,
                    price: item.price,
                    // Không gửi userId vì sẽ được lấy từ token
                }))
            };
            
            console.log("Final orderDTO:", orderDTO);
            
            const response = await axios.post(`${this.BASE_URL}/order/create`, orderDTO, {
                headers: this.getHeader()
            });
            
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

    static async createZaloPayOrder(paymentData) {
        try {
            console.log("Creating ZaloPay order:", paymentData);
            
            // Validate input
            if (!paymentData.orderId || !paymentData.amount) {
                throw new Error("Missing required parameters: orderId or amount");
            }
            
            // Đảm bảo amount là số
            const amount = parseFloat(paymentData.amount);
            if (isNaN(amount)) {
                throw new Error("Invalid amount format");
            }
            
            // Gọi API với đúng định dạng tham số
            const response = await axios.post(`${this.BASE_URL}/zalopay/create-order`, {
                amount: amount,
                orderID: paymentData.orderId,
                description: `Payment for order #${paymentData.orderId}`
            }, {
                headers: this.getHeader(),
                withCredentials: true
            });
            
            console.log("ZaloPay API response:", response.data);
            
            // Kiểm tra response
            if (!response.data) {
                throw new Error("Empty response from ZaloPay API");
            }
            
            // Kiểm tra nếu ZaloPay API trả về lỗi
            if (response.data.error) {
                throw new Error("ZaloPay API error: " + response.data.error);
            }
            
            // Đảm bảo response có chứa order_url
            if (!response.data.order_url) {
                console.error("No order_url in ZaloPay response:", response.data);
                throw new Error("Missing payment URL in response");
            }
            
            return response.data;
        } catch (error) {
            console.error("ZaloPay error:", error);
            
            // Xử lý các loại lỗi khác nhau
            if (error.response) {
                // Lỗi từ server với response
                console.error("Server error data:", error.response.data);
                throw new Error(error.response.data.error || "Server error: " + error.response.status);
            } else if (error.request) {
                // Không nhận được response
                throw new Error("No response from server. Please check your network connection.");
            } else {
                // Lỗi khác
                throw error;
            }
        }
    }

    static async checkZaloPayStatus(appTransId) {
        try {
            const response = await axios.get(`${this.BASE_URL}/zalopay/order-status/${appTransId}`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            console.error("Error checking ZaloPay status:", error);
            throw error;
        }
    }

    static async getAllOrders() {
        try {
            const response = await axios.get(`${this.BASE_URL}/order/all`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            console.error("Error fetching all orders:", error);
            throw error;
        }
    }

    static async getUserOrders() {
        try {
            if (this.isSessionExpired()) {
                throw new Error('Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.');
            }
            
            const response = await axios.get(`${this.BASE_URL}/order/user`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            if (error.message === 'Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.') {
                // Xử lý đăng xuất và chuyển hướng
                this.logout();
                window.location.href = '/login?expired=true';
            }
            console.error("Error fetching user orders:", error);
            throw error;
        }
    }

    static async getOrdersByStatus(status) {
        try {
            const response = await axios.get(`${this.BASE_URL}/order/status/${status}`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            console.error(`Error fetching orders by status ${status}:`, error);
            throw error;
        }
    }

    static async updateOrderStatus(orderId, status) {
        try {
            const response = await axios.put(`${this.BASE_URL}/order/${orderId}/status?status=${status}`, null, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            console.error("Error updating order status:", error);
            throw error;
        }
    }

    static async updatePaymentStatus(orderId, status) {
        try {
            const response = await axios.put(`${this.BASE_URL}/order/${orderId}/payment-status?status=${status}`, null, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            console.error("Error updating payment status:", error);
            throw error;
        }
    }

    static async getOrderStatus(orderId) {
        try {
            console.log("Checking status for order:", orderId);
            
            // Sử dụng endpoint mới /order/order-status/
            const response = await axios({
                method: 'get',
                url: `${this.BASE_URL}/order/order-status/${orderId}`,
                headers: this.getHeader(),
                withCredentials: true,
                timeout: 10000
            });
            
            console.log("Order status API response:", response.data);
            
            if (response && response.data) {
                return {
                    success: true,
                    status: response.data.status || 'UNKNOWN',
                    paymentStatus: response.data.paymentStatus || 'UNKNOWN',
                    lastUpdated: response.data.lastUpdated,
                    orderId: response.data.orderId || orderId
                };
            }
            
            return {
                success: false,
                status: 'UNKNOWN',
                paymentStatus: 'UNKNOWN',
                error: 'Invalid response format'
            };
        } catch (error) {
            console.error('Error checking order status:', error);
            
            return {
                success: false,
                status: 'ERROR',
                paymentStatus: 'ERROR',
                error: error.message || 'Failed to check order status'
            };
        }
    }

    static async handlePaymentCallback(callbackData) {
        try {
            const response = await axios.post(`${this.BASE_URL}/payment/callback`, callbackData, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            console.error("Payment callback error:", error);
            throw error;
        }
    }

    static async getOrderById(orderId) {
        try {
            // Sử dụng URL đúng cho API lấy chi tiết đơn hàng
            const response = await axios.get(`${this.BASE_URL}/order/status/${orderId}`, {
                headers: this.getHeader()
            });
            return response.data;
        } catch (error) {
            console.error(`Error fetching order by ID:`, error);
            throw error;
        }
    }

    // Thêm phương thức kiểm tra kết nối
    static async checkConnection() {
        try {
            const response = await axios.get(`${this.BASE_URL}/api/health`, { timeout: 5000 });
            return response.status === 200;
        } catch (error) {
            console.error("Connection check failed:", error);
            return false;
        }
    }

    // Thêm phương thức để kiểm tra phiên làm việc
    static isSessionExpired() {
        const token = localStorage.getItem('token');
        if (!token) return true;
        
        // Giải mã JWT để kiểm tra thời gian hết hạn
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            
            const { exp } = JSON.parse(jsonPayload);
            
            // Kiểm tra xem token đã hết hạn chưa
            return exp * 1000 < Date.now();
        } catch (e) {
            console.error('Error parsing token:', e);
            return true; // Nếu không thể phân tích token, coi như đã hết hạn
        }
    }

    static async smartSearch(searchQuery) {
        try {
            const response = await axios.post(
                `${this.BASE_URL}/api/products/smart-search`,
                searchQuery,
                {
                    headers: {
                        'Content-Type': 'text/plain'
                    }
                }
            );
            
            // Kiểm tra và xử lý response
            if (response.data && Array.isArray(response.data.products)) {
                return {
                    products: response.data.products,
                    explanation: response.data.explanation || '',
                    criteria: response.data.criteria
                };
            }
            
            // Nếu response không đúng format
            console.warn('Unexpected response format:', response.data);
            return {
                products: [],
                explanation: "Định dạng dữ liệu không hợp lệ",
                criteria: null
            };
            
        } catch (error) {
            console.error('Smart search error:', error);
            // Trả về object rỗng với message lỗi cụ thể
            return {
                products: [],
                explanation: error.response?.data?.message || "Có lỗi xảy ra khi tìm kiếm",
                criteria: null
            };
        }
    }
}