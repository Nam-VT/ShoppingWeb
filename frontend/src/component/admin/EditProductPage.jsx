import React, { useState, useEffect, useCallback } from "react";
import { useNavigate, useParams } from "react-router-dom";
import '../../style/addProduct.css'
import ApiService from "../../service/ApiService";

// Thêm BASE_URL trực tiếp trong component
const BASE_URL = 'http://localhost:8080';

const EditProductPage = () => {
    const { productId } = useParams();
    const [formData, setFormData] = useState({
        categoryId: '',
        name: '',
        description: '',
        price: '',
        image: null,
        currentImageUrl: ''
    });
    const [categories, setCategories] = useState([]);
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const fetchCategories = async () => {
        try {
            const response = await ApiService.getAllCategory();
            setCategories(response || []);
        } catch (error) {
            setMessage(error.message || 'Failed to load categories');
        }
    };

    const fetchProduct = useCallback(async () => {
        try {
            const response = await ApiService.getProductById(productId);
            setFormData({
                categoryId: response.categories[0]?.id || '',
                name: response.name,
                description: response.description,
                price: response.price,
                currentImageUrl: response.imageUrl
            });
        } catch (error) {
            setMessage(error.message || 'Failed to load product');
        }
    }, [productId]);

    useEffect(() => {
        fetchCategories();
        if (productId) {
            fetchProduct();
        }
    }, [fetchProduct, productId]);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleFileChange = (e) => {
        setFormData(prev => ({
            ...prev,
            image: e.target.files[0]
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Validate
        if (!formData.categoryId || !formData.name || !formData.description || !formData.price) {
            setMessage('All fields are required');
            return;
        }

        // Validate price
        if (Number(formData.price) <= 0) {
            setMessage('Price must be greater than 0');
            return;
        }

        setLoading(true);
        try {
            // Tạo object với kiểu dữ liệu đã convert
            const productData = {
                productId: Number(productId),
                categoryId: Number(formData.categoryId),
                name: formData.name.trim(),
                description: formData.description.trim(),
                price: Number(formData.price)
            };

            // Log data đã convert
            console.log('=== DEBUG: Data After Convert ===');
            console.log('Product Data:', productData);

            const formDataToSend = new FormData();
            // Append data đã convert - ensure values are strings for FormData
            Object.entries(productData).forEach(([key, value]) => {
                formDataToSend.append(key, String(value));
            });
            
            // Append image nếu có
            if (formData.image) {
                formDataToSend.append('image', formData.image);
            }

            // Log FormData
            console.log('=== DEBUG: FormData Content ===');
            for (let [key, value] of formDataToSend.entries()) {
                console.log(`${key}:`, value, `(type: ${typeof value === 'object' ? 'File' : typeof value})`);
            }

            const response = await ApiService.updateProduct(formDataToSend);
            console.log('=== DEBUG: Update Success ===');
            console.log('Response:', response);
            
            setMessage('Product updated successfully');
            setTimeout(() => navigate('/admin/products'), 2000);
        } catch (error) {
            console.error('=== DEBUG: Submit Error ===');
            console.error('Error Status:', error.response?.status);
            console.error('Error Message:', error.response?.data?.message || error.response?.data);
            console.error('Error Details:', error.response?.data);
            
            // Set more informative error message
            if (error.response?.data) {
                setMessage(typeof error.response.data === 'string' ? error.response.data : 
                           error.response.data.message || 'Failed to update product');
            } else {
                setMessage('Network error or server is unreachable');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="product-form" encType="multipart/form-data">
            {formData.currentImageUrl && (
                <div className="current-image">
                    <img 
                        src={`${BASE_URL}${formData.currentImageUrl}`} 
                        alt="Current product" 
                        style={{maxWidth: '200px'}}
                    />
                </div>
            )}
            
            <input 
                type="file"
                name="image"
                accept="image/*"
                onChange={handleFileChange}
                disabled={loading}
            />

            <h2>Edit Product</h2>
            {message && <div className="message">{message}</div>}

            <select 
                name="categoryId"
                value={formData.categoryId}
                onChange={handleChange}
                disabled={loading}
            >
                <option value="">Select Category</option>
                {categories.map((cat) => (
                    <option value={cat.id} key={cat.id}>{cat.name}</option>
                ))}
            </select>

            <input 
                type="text"
                name="name"
                placeholder="Product name"
                value={formData.name}
                onChange={handleChange}
                disabled={loading}
            />

            <textarea 
                name="description"
                placeholder="Description"
                value={formData.description}
                onChange={handleChange}
                disabled={loading}
            />

            <input 
                type="number"
                name="price"
                placeholder="Price"
                value={formData.price}
                onChange={handleChange}
                step="0.01"
                min="0"
                disabled={loading}
            />

            <button type="submit" disabled={loading}>
                {loading ? 'Updating...' : 'Update Product'}
            </button>
        </form>
    );
};

export default EditProductPage;