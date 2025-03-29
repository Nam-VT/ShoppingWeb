import axios from 'axios';

export default class ApiService {

    static BASE_URL = "https://localhost:8080";

    static getHeaders() {
        const token = localStorage.getItem("token");
        return {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
        };
    }

    /* Auth and User APIs */
    static async registerUser(registration) {
        const response = await axios.post(`${this.BASE_URL}/auth/register`, registration);
        return response.data;
    }
    
    static async loginUser(loginDetails) {
        const response = await axios.post(`${this.BASE_URL}/auth/login`, loginDetails);
        return response.data;
    }

    static async getLoggedUserInfo() {
        const response = await axios.get(`${this.BASE_URL}/auth/login`, { headers: this.getHeaders() });
        return response.data;
    }

    
}    