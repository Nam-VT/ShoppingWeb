import React, { useState, useEffect } from "react";
import ApiService from "../../service/ApiService";
import { useNavigate, useParams } from "react-router-dom";
import '../../style/addCategory.css'

const EditCategory = () => {
    const { categoryId } = useParams();
    const [name, setName] = useState('');
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        fetchCategory();
    }, [categoryId]);

    const fetchCategory = async () => {
        try {
            const response = await ApiService.getCategoryById(categoryId);
            setName(response.name); // Trực tiếp lấy name từ response
        } catch (error) {
            setMessage(error.message || "Failed to get category");
            setTimeout(() => setMessage(''), 3000);
        }
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (!name.trim()) {
            setMessage('Category name is required');
            return;
        }

        setLoading(true);
        try {
            await ApiService.updateCategory(categoryId, name.trim());
            setMessage('Category updated successfully');
            setTimeout(() => navigate("/admin/categories"), 2000);
        } catch (error) {
            setMessage(error.message || "Failed to update category");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="add-category-page">
            {message && <p className="message">{message}</p>}
            <form onSubmit={handleSubmit} className="category-form">
                <h2>Edit Category</h2>
                <input 
                    type="text"
                    placeholder="Category Name"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    disabled={loading}
                />
                <button type="submit" disabled={loading}>
                    {loading ? 'Updating...' : 'Update'}
                </button>
            </form>
        </div>
    );
}

export default EditCategory;