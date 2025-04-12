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
        const response = await axios.get(`${this.BASE_URL}/user/info`, {
            headers: this.getHeader()
        });
        return response.data;
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
        const response = await axios.post(`${this.BASE_URL}/product/create`, null, {
            headers: this.getHeader(),
            params: {
                categoryId: formData.categoryId,
                name: formData.name,
                description: formData.description,
                price: formData.price
            }
        });
        return response.data;
    }

    static async updateProduct(formData) {
        const response = await axios.put(`${this.BASE_URL}/product/update`, null, {
            headers: this.getHeader(),
            params: {
                productId: formData.productId,
                categoryId: formData.categoryId,
                name: formData.name,
                description: formData.description,
                price: formData.price
            }
        });
        return response.data;
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
        const response = await axios.post(`${this.BASE_URL}/address/save`, address, {
            headers: this.getHeader()
        });
        return response.data;
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
}

ApiService.setupAxiosInterceptors();