import React, { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import '../../style/adminProduct.css'
import Pagination from "../common/Pagination";
import ApiService from "../../service/ApiService";

// Thêm BASE_URL trực tiếp 
const BASE_URL = 'http://localhost:8080';

const AdminProductPage = () => {
    const navigate = useNavigate();
    const [products, setProducts] = useState([]);
    const [currentPage, setCurrentPage] = useState(1);
    const [totalPages, setTotalPages] = useState(0);
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);
    const itemsPerPage = 10;

    // Wrap fetchProducts trong useCallback
    const fetchProducts = useCallback(async () => {
        setLoading(true);
        try {
            const response = await ApiService.getAllProducts();
            const productList = response || [];
            setTotalPages(Math.ceil(productList.length / itemsPerPage));
            setProducts(productList.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage));
        } catch (error) {
            setMessage(error.message || 'Failed to fetch products');
        } finally {
            setLoading(false);
        }
    }, [currentPage, itemsPerPage]);

    useEffect(() => {
        fetchProducts();
    }, [fetchProducts]);

    const handleEdit = (id) => {
        navigate(`/admin/edit-product/${id}`);
    };

    const handleDelete = async (id) => {
        if (!window.confirm("Are you sure you want to delete this product?")) {
            return;
        }

        try {
            await ApiService.deleteProduct(id);
            setMessage('Product deleted successfully');
            fetchProducts(); // Refresh list
        } catch (error) {
            setMessage(error.message || 'Failed to delete product');
        }

        // Clear message after 3 seconds
        setTimeout(() => setMessage(''), 3000);
    };

    if (loading) {
        return <div>Loading...</div>;
    }

    return (
        <div className="admin-product-list">
            {message && <p className="message">{message}</p>}
            
            <div>
                <h2>Products</h2>
                <button 
                    className="product-btn" 
                    onClick={() => navigate('/admin/add-product')}
                >
                    Add Product
                </button>

                <ul>
                    {products.map((product) => (
                        <li key={product.id}>
                            <img 
                                src={`${BASE_URL}${product.imageUrl}`}
                                alt={product.name}
                                style={{width: '50px', height: '50px', objectFit: 'cover'}}
                                onError={(e) => {
                                    console.log('Image URL:', `${BASE_URL}${product.imageUrl}`);
                                    console.log('Load image error:', e);
                                }}
                            />
                            <span>{product.name}</span>
                            <button 
                                className="product-btn" 
                                onClick={() => handleEdit(product.id)}
                            >
                                Edit
                            </button>
                            <button 
                                className="product-btn-delete" 
                                onClick={() => handleDelete(product.id)}
                            >
                                Delete
                            </button>
                        </li>
                    ))}
                </ul>

                <Pagination
                    currentPage={currentPage}
                    totalPages={totalPages}
                    onPageChange={(page) => setCurrentPage(page)}
                />
            </div>
        </div>
    );
};

export default AdminProductPage;