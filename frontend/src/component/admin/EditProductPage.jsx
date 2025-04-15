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

        if (!formData.categoryId || !formData.name || !formData.description || !formData.price) {
            setMessage('All fields are required');
            return;
        }

        setLoading(true);
        try {
            const productData = {
                productId: parseInt(productId),
                categoryId: [parseInt(formData.categoryId)],
                name: formData.name.trim(),
                description: formData.description.trim(),
                price: parseFloat(formData.price)
            };

            await ApiService.updateProduct(productData);
            setMessage('Product updated successfully');
            setTimeout(() => navigate('/admin/products'), 2000);
        } catch (error) {
            setMessage(error.message || 'Failed to update product');
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