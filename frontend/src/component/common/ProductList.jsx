import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";
import '../../style/productList.css';

// Thêm BASE_URL 
const BASE_URL = 'http://localhost:8080';

const ProductList = ({products}) => {
    const {cart, dispatch} = useCart();
    const navigate = useNavigate();

    console.log("Products in ProductList:", products);

    const addToCart = (product) => {
        if (product.stockQuantity > 0) {
            dispatch({type: 'ADD_ITEM', payload: product});
        } else {
            alert('Sản phẩm đã hết hàng!');
        }
    }

    const goToProductDetail = (productId) => {
        navigate(`/product/${productId}`);
    }

    return(
        <div className="product-list">
            {products.map((product, index) => {
                console.log("Product image URL:", `${BASE_URL}${product.imageUrl}`);
                const isInCart = cart.some(item => item.id === product.id);
                
                return (
                    <div className="product-item" key={index}>
                        <div className="product-image-container">
                            <img 
                                src={`${BASE_URL}${product.imageUrl}`}
                                alt={product.name} 
                                className="product-image"
                                onError={(e) => {
                                    e.target.src = '/images/placeholder.png';
                                }}
                                loading="lazy"
                            />
                        </div>
                        
                        <div className="product-info">
                            <h3>{product.name}</h3>
                            <p className="product-description">{product.description.length > 50 
                                ? `${product.description.substring(0, 50)}...` 
                                : product.description}</p>
                            <span className="product-price">${product.price.toFixed(2)}</span>
                        </div>
                        
                        <div className="product-actions">
                            <button 
                                className={`add-to-cart-btn ${isInCart ? 'added' : ''}`}
                                onClick={() => addToCart(product)}
                                disabled={isInCart}
                            >
                                {isInCart ? 'Added to Cart' : 'Add to Cart'}
                            </button>
                            
                            <button 
                                className="more-info-btn"
                                onClick={() => goToProductDetail(product.id)}
                            >
                                More Info
                            </button>
                        </div>
                    </div>
                )
            })}
        </div>
    )
};

export default ProductList;