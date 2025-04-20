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
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [newReview, setNewReview] = useState({
        rating: 5,
        comment: ""
    });
    const [submitting, setSubmitting] = useState(false);
    const [reviewMessage, setReviewMessage] = useState("");
    const [hasReviewed, setHasReviewed] = useState(false);
    
    useEffect(() => {
        fetchProduct();
        fetchReviews();
    }, [productId]);

    const fetchProduct = async () => {
        try {
            setLoading(true);
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
    
    const fetchReviews = async () => {
        try {
            const data = await ApiService.getProductReviews(productId);
            setReviews(data || []);
            
            if (ApiService.isAuthenticated()) {
                const currentUser = await ApiService.getLoggedInUserInfo();
                const userReview = data.find(review => 
                    review.user && review.user.id === currentUser.id
                );
                setHasReviewed(!!userReview);
            }
        } catch (error) {
            console.error("Error fetching reviews:", error);
            setReviews([]);
        }
    };
    
    const handleReviewChange = (e) => {
        const { name, value } = e.target;
        setNewReview(prev => ({
            ...prev,
            [name]: name === 'rating' ? parseInt(value) : value
        }));
    };
    
    const submitReview = async (e) => {
        e.preventDefault();
        
        if (!newReview.comment.trim()) {
            setReviewMessage("Please enter a comment");
            return;
        }
        
        setSubmitting(true);
        try {
            if (!ApiService.isAuthenticated()) {
                setReviewMessage("Please login to submit a review");
                setSubmitting(false);
                return;
            }
            
            const reviewData = {
                productId: productId,
                rating: newReview.rating,
                comment: newReview.comment
            };
            
            await ApiService.createReview(reviewData);
            setReviewMessage("Review submitted successfully!");
            setNewReview({ rating: 5, comment: "" });
            
            // Refresh reviews
            fetchReviews();
        } catch (error) {
            console.error("Error submitting review:", error);
            
            let errorMessage = "Failed to submit review. Please try again later.";
            
            // Trích xuất thông báo lỗi từ response của API
            if (error.response) {
                if (error.response.data && typeof error.response.data === 'object' && error.response.data.message) {
                    // Nếu API trả về object với trường message (ApiError)
                    errorMessage = error.response.data.message;
                } else if (error.response.data && typeof error.response.data === 'string') {
                    // Nếu API trả về string trực tiếp
                    errorMessage = error.response.data;
                }
                
                // Xử lý cụ thể cho trường hợp "already reviewed"
                if (errorMessage.includes("already reviewed")) {
                    errorMessage = "You have already reviewed this product.";
                }
            }
            
            setReviewMessage(errorMessage);
        } finally {
            setSubmitting(false);
            
            // Xóa thông báo sau 5 giây nếu là thông báo thành công
            if (reviewMessage.includes("successfully")) {
                setTimeout(() => {
                    setReviewMessage("");
                }, 5000);
            }
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

    // Render stars for rating
    const renderStars = (rating) => {
        const stars = [];
        for (let i = 1; i <= 5; i++) {
            stars.push(
                <span key={i} className={i <= rating ? "star filled" : "star"}>
                    ★
                </span>
            );
        }
        return stars;
    };

    return(
        <div className="product-detail-page">
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
            
            <div className="product-reviews">
                <h2>Customer Reviews</h2>
                
                <div className="review-form">
                    <h3>Write a Review</h3>
                    
                    {reviewMessage && <div className="review-message">{reviewMessage}</div>}
                    
                    {!ApiService.isAuthenticated() ? (
                        <div className="login-required">
                            Please <a href="/login">log in</a> to write a review
                        </div>
                    ) : hasReviewed ? (
                        <div className="already-reviewed">
                            You have already reviewed this product
                        </div>
                    ) : (
                        <form onSubmit={submitReview}>
                            <div className="rating-select">
                                <label>Rating:</label>
                                <select 
                                    name="rating" 
                                    value={newReview.rating} 
                                    onChange={handleReviewChange}
                                    disabled={submitting}
                                >
                                    <option value="5">5 - Excellent</option>
                                    <option value="4">4 - Very Good</option>
                                    <option value="3">3 - Good</option>
                                    <option value="2">2 - Fair</option>
                                    <option value="1">1 - Poor</option>
                                </select>
                            </div>
                            
                            <div className="comment-input">
                                <label>Comment:</label>
                                <textarea 
                                    name="comment"
                                    value={newReview.comment}
                                    onChange={handleReviewChange}
                                    disabled={submitting}
                                    placeholder="Share your thoughts about this product..."
                                    rows="4"
                                ></textarea>
                            </div>
                            
                            <button 
                                type="submit" 
                                className="submit-review" 
                                disabled={submitting}
                            >
                                {submitting ? "Submitting..." : "Submit Review"}
                            </button>
                        </form>
                    )}
                </div>
                
                <div className="reviews-list">
                    {reviews.length === 0 ? (
                        <p className="no-reviews">No reviews yet. Be the first to review this product!</p>
                    ) : (
                        reviews.map((review, index) => (
                            <div className="review-item" key={index}>
                                <div className="review-header">
                                    <span className="review-author">{review.customerName}</span>
                                    <div className="review-rating">
                                        {renderStars(review.rating)}
                                    </div>
                                </div>
                                <div className="review-comment">{review.comment}</div>
                                <div className="review-date">
                                    {new Date(review.createdAt).toLocaleDateString()}
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </div>
        </div>
    );
};

export default ProductDetailsPage;