import React, { useState } from "react";
import ApiService from "../../service/ApiService";
import { useNavigate } from "react-router-dom";
import '../../style/addCategory.css';

const AddCategory = () => {
    const [name, setName] = useState('');
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!name.trim()) {
            setMessage('Category name is required');
            return;
        }

        if (name.length < 2) {
            setMessage('Category name must be at least 2 characters');
            return;
        }

        setLoading(true);
        try {
            await ApiService.createCategory({ name: name.trim() });
            setMessage('Category created successfully');
            setTimeout(() => {
                navigate("/admin/categories");
            }, 2000);
        } catch (error) {
            const errorMessage = error.response?.data?.message 
                || error.message 
                || 'Failed to create category';
            setMessage(errorMessage);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="add-category-page">
            {message && <p className="message">{message}</p>}
            <form onSubmit={handleSubmit} className="category-form">
                <h2>Add Category</h2>
                <input 
                    type="text"
                    placeholder="Category Name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    disabled={loading}
                />
                <button type="submit" disabled={loading}>
                    {loading ? 'Creating...' : 'Add'}
                </button>
            </form>
        </div>
    );
};

export default AddCategory;