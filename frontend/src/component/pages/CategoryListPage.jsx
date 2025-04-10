import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import ApiService from "../../service/ApiService";
import '../../style/categoryListPage.css'

const CategoryListPage = () => {
    const [categories, setCategories] = useState([]);
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        fetchCategories();
    }, []);

    const fetchCategories = async () => {
        setLoading(true);
        try {
            const response = await ApiService.getAllCategory();
            setCategories(response || []);
        } catch (err) {
            setError(err.response?.data?.message || err.message || 'Unable to fetch categories');
        } finally {
            setLoading(false);
        }
    }

    const handleCategoryClick = (categoryId) => {
        navigate(`/category/${categoryId}`);
    } 

    return(
        <div className="category-list">
            {loading ? (
                <p>Loading categories...</p>
            ) : error ? (
                <p className="error-message">{error}</p>
            ) : (
                <div>
                    <h2>Categories</h2>
                    {categories.length === 0 ? (
                        <p>No categories found</p>
                    ) : (
                        <ul>
                            {categories.map((category)=>(
                                <li key={category.id}>
                                    <button onClick={()=> handleCategoryClick(category.id)}>{category.name}</button>
                                </li>
                            ))}
                        </ul>
                    )}
                </div>
            )}
        </div>
    )
}

export default CategoryListPage;