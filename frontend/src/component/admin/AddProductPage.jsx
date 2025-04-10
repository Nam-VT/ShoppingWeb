import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import '../../style/addProduct.css'
import ApiService from "../../service/ApiService";

const AddProductPage = () => {
    const [formData, setFormData] = useState({
        categoryId: '',
        name: '',
        description: '',
        price: ''
    });
    const [categories, setCategories] = useState([]);
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        fetchCategories();
    }, []);

    const fetchCategories = async () => {
        try {
            const response = await ApiService.getAllCategory();
            setCategories(response || []);
        } catch (error) {
            setMessage(error.message || 'Failed to load categories');
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
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
                categoryId: [parseInt(formData.categoryId)],
                name: formData.name.trim(),
                description: formData.description.trim(),
                price: parseFloat(formData.price)
            };

            await ApiService.addProduct(productData);
            setMessage('Product added successfully');
            setTimeout(() => navigate('/admin/products'), 2000);
        } catch (error) {
            setMessage(error.message || 'Failed to add product');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div>
            <form onSubmit={handleSubmit} className="product-form">
                <h2>Add Product</h2>
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
                    {loading ? 'Adding...' : 'Add Product'}
                </button>
            </form>
        </div>
    );
};

export default AddProductPage;