import React, {useEffect, useState} from "react";
import { useParams } from "react-router-dom";
import { useCart } from "../context/CartContext";
import ApiService from "../../service/ApiService";
import '../../style/productDetailsPage.css';

const BASE_URL = 'http://localhost:8080';

const ProductDetailsPage = () => {
    const {productId} = useParams();
    const {cart, dispatch} = useCart();
    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchProduct();
    }, [productId]);

    const fetchProduct = async () => {
        try {
            setLoading(true);
            setError(null);
            console.log("Fetching product with ID:", productId);
            
            const response = await ApiService.getProductById(productId);
            console.log("API Response:", response);
            
            if (response) {
                setProduct(response);
            } else {
                setError("Product not found");
            }
        } catch (error) {
            console.error("Error fetching product:", error);
            setError(error.message || "Failed to load product");
        } finally {
            setLoading(false);
        }
    };
    
    const addToCart = () => {
        if (product) {
            dispatch({type: 'ADD_ITEM', payload: product});   
        }
    };

    const incrementItem = () => {
        if(product){
            dispatch({type: 'INCREMENT_ITEM', payload: product});
        }
    };

    const decrementItem = () => {
        if (product) {
            const cartItem = cart.find(item => item.id === product.id);
            if (cartItem && cartItem.quantity > 1) {
                dispatch({type: 'DECREMENT_ITEM', payload: product}); 
            } else {
                dispatch({type: 'REMOVE_ITEM', payload: product}); 
            }
        }
    };

    if (loading) {
        return <div className="loading">Loading product details...</div>;
    }

    if (error) {
        return <div className="error">Error: {error}</div>;
    }

    if (!product) {
        return <div className="not-found">Product not found</div>;
    }

    const cartItem = cart.find(item => item.id === product.id);
    const imageUrl = product.imageUrl ? `${BASE_URL}${product.imageUrl}` : '/placeholder-image.jpg';

    return(
        <div className="product-detail">
            <div className="product-image">
                <img src={imageUrl} alt={product.name} />
            </div>
            
            <div className="product-info">
                <h1 className="product-name">{product.name}</h1>
                
                <div className="product-category">
                    Category: {product.categories && product.categories.length > 0 
                        ? product.categories[0].name 
                        : 'Uncategorized'}
                </div>
                
                <div className="product-price">
                    Price: ${product.price.toFixed(2)}
                </div>
                
                <div className="product-description">
                    <h3>Description:</h3>
                    <p>{product.description}</p>
                </div>
                
                <div className="product-stock">
                    In Stock: {product.stockQuantity || 0}
                </div>
                
                <div className="product-actions">
                    {cartItem ? (
                        <div className="quantity-controls">
                            <button className="decrement" onClick={decrementItem}>-</button>
                            <span className="quantity">{cartItem.quantity}</span>
                            <button className="increment" onClick={incrementItem}>+</button>
                        </div>
                    ) : (
                        <button className="add-to-cart" onClick={addToCart}>Add To Cart</button>
                    )}
                </div>
            </div>
        </div>
    );
};

export default ProductDetailsPage;