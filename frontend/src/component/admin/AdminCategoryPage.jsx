import React, { useState, useEffect } from "react";
import ApiService from "../../service/ApiService";
import { useNavigate } from "react-router-dom";
import '../../style/adminCategory.css'

const AdminCategoryPage = () => {
    const [categories, setCategories] = useState([]);
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        fetchCategories();
    }, []);

    const fetchCategories = async () => {
        setLoading(true);
        try {
            const response = await ApiService.getAllCategory();
            setCategories(response || []);
        } catch (error) {
            setMessage(error.message || "Failed to fetch categories");
        } finally {
            setLoading(false);
        }
    };

    const handleEdit = (id) => {
        navigate(`/admin/edit-category/${id}`);
    };

    const handleDelete = async (id) => {
        if (!window.confirm("Are you sure you want to delete this category?")) {
            return;
        }

        try {
            await ApiService.deleteCategory(id);
            setMessage('Category deleted successfully');
            fetchCategories(); // Refresh list
        } catch (error) {
            setMessage(error.message || "Failed to delete category");
        }
        
        // Clear message after 3 seconds
        setTimeout(() => setMessage(''), 3000);
    };

    if (loading) {
        return <div>Loading...</div>;
    }

    return (
        <div className="admin-category-page">
            <div className="admin-category-list">
                <h2>Categories</h2>
                {message && <p className="message">{message}</p>}
                
                <button onClick={() => navigate('/admin/add-category')}>
                    Add Category
                </button>

                <ul>
                    {categories.map((category) => (
                        <li key={category.id}>
                            <span>{category.name}</span>
                            <div className="admin-bt">
                                <button 
                                    className="admin-btn-edit" 
                                    onClick={() => handleEdit(category.id)}
                                >
                                    Edit
                                </button>
                                <button 
                                    onClick={() => handleDelete(category.id)}
                                >
                                    Delete
                                </button>
                            </div>
                        </li>
                    ))}
                </ul>
            </div>
        </div>
    );
};

export default AdminCategoryPage;