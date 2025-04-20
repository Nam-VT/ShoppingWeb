import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import ApiService from "../../service/ApiService";
import { useCart } from "../context/CartContext";
import '../../style/cart.css'

// Thêm BASE_URL
const BASE_URL = 'http://localhost:8080';

const CartPage = () => {
    const { cart, dispatch } = useCart();
    const [message, setMessage] = useState(null);
    const [userAddress, setUserAddress] = useState(null);
    const [paymentMethod, setPaymentMethod] = useState('CASH'); // Mặc định là tiền mặt
    const [showCheckoutForm, setShowCheckoutForm] = useState(false);
    const navigate = useNavigate();

    // Lấy thông tin địa chỉ người dùng
    useEffect(() => {
        const fetchUserInfo = async () => {
            if (ApiService.isAuthenticated()) {
                try {
                    const userInfo = await ApiService.getLoggedInUserInfo();
                    if (userInfo && userInfo.address) {
                        setUserAddress(userInfo.address);
                    }
                } catch (error) {
                    console.error("Error fetching user info:", error);
                }
            }
        };
        
        fetchUserInfo();
    }, []);

    const incrementItem = (product) => {
        dispatch({ type: 'INCREMENT_ITEM', payload: product });
    }

    const decrementItem = (product) => {
        const cartItem = cart.find(item => item.id === product.id);
        if (cartItem && cartItem.quantity > 1) {
            dispatch({ type: 'DECREMENT_ITEM', payload: product });
        } else {
            dispatch({ type: 'REMOVE_ITEM', payload: product });
        }
    }

    const totalPrice = cart.reduce((total, item) => total + item.price * item.quantity, 0);

    const initiateCheckout = () => {
        if (!ApiService.isAuthenticated()) {
            setMessage("Vui lòng đăng nhập để tiếp tục");
            setTimeout(() => {
                navigate("/login");
            }, 2000);
            return;
        }
        
        setShowCheckoutForm(true);
    }

    const handleAddressChange = (e) => {
        const { name, value } = e.target;
        setUserAddress(prev => ({
            ...prev,
            [name]: value
        }));
    }

    const handleCheckout = async () => {
        if (!userAddress || !userAddress.street || !userAddress.city) {
            setMessage("Vui lòng nhập đầy đủ thông tin địa chỉ");
            return;
        }

        const orderItems = cart.map(item => ({
            productId: item.id,
            quantity: item.quantity
        }));

        const orderRequest = {
            totalPrice,
            items: orderItems,
            paymentMethod,
            shippingAddress: `${userAddress.street}, ${userAddress.city}, ${userAddress.state || ''}, ${userAddress.country || ''}`,
            status: paymentMethod === 'CASH' ? 'PENDING' : 'PROCESSING'
        }

        try {
            // Lưu địa chỉ người dùng nếu cần
            if (userAddress && userAddress.id === undefined) {
                await ApiService.saveAddress(userAddress);
            }
            
            const response = await ApiService.createOrder(orderRequest);
            
            if (response.status === 200 || response.status === 201) {
                if (paymentMethod === 'CASH') {
                    setMessage('Đặt hàng thành công!');
                    dispatch({ type: 'CLEAR_CART' });
                } else if (paymentMethod === 'VNPAY') {
                    // Nếu là VNPAY, cần tạo URL thanh toán và chuyển hướng
                    const paymentUrl = await ApiService.createVNPayUrl(response.orderId, totalPrice);
                    window.location.href = paymentUrl; // Chuyển hướng đến trang thanh toán VNPAY
                }
            }
            
            setShowCheckoutForm(false);
        } catch (error) {
            setMessage(error.response?.data?.message || error.message || 'Không thể tạo đơn hàng');
            setTimeout(() => {
                setMessage('');
            }, 3000);
        }
    };

    return (
        <div className="cart-page">
            <h1>Giỏ hàng</h1>
            {message && <p className="response-message">{message}</p>}

            {cart.length === 0 ? (
                <p>Giỏ hàng của bạn đang trống</p>
            ) : (
                <div>
                    {!showCheckoutForm ? (
                        <>
                            <ul>
                                {cart.map(item => (
                                    <li key={item.id}>
                                        <img 
                                            src={`${BASE_URL}${item.imageUrl}`}
                                            alt={item.name}
                                            onError={(e) => {
                                                e.target.src = '/images/placeholder.png';
                                            }} 
                                        />
                                        <div>
                                            <h2>{item.name}</h2>
                                            <p>{item.description}</p>
                                            <div className="quantity-controls">
                                                <button onClick={()=> decrementItem(item)}>-</button>
                                                <span>{item.quantity}</span>
                                                <button onClick={()=> incrementItem(item)}>+</button>
                                            </div>
                                            <span>${item.price.toFixed(2)}</span>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                            <h2>Tổng cộng: ${totalPrice.toFixed(2)}</h2>
                            <button className="checkout-button" onClick={initiateCheckout}>Thanh toán</button>
                        </>
                    ) : (
                        <div className="checkout-form">
                            <h2>Thông tin đặt hàng</h2>
                            
                            <div className="order-summary">
                                <h3>Tóm tắt đơn hàng</h3>
                                <ul>
                                    {cart.map(item => (
                                        <li key={item.id}>
                                            <span>{item.name} x {item.quantity}</span>
                                            <span>${(item.price * item.quantity).toFixed(2)}</span>
                                        </li>
                                    ))}
                                </ul>
                                <div className="total">
                                    <strong>Tổng cộng:</strong>
                                    <strong>${totalPrice.toFixed(2)}</strong>
                                </div>
                            </div>
                            
                            <div className="address-form">
                                <h3>Địa chỉ giao hàng</h3>
                                <div className="form-row">
                                    <input
                                        type="text"
                                        name="street"
                                        placeholder="Địa chỉ đường phố"
                                        value={userAddress?.street || ''}
                                        onChange={handleAddressChange}
                                    />
                                </div>
                                <div className="form-row">
                                    <input
                                        type="text"
                                        name="city"
                                        placeholder="Thành phố"
                                        value={userAddress?.city || ''}
                                        onChange={handleAddressChange}
                                    />
                                </div>
                                <div className="form-row">
                                    <input
                                        type="text"
                                        name="state"
                                        placeholder="Tỉnh/Thành phố"
                                        value={userAddress?.state || ''}
                                        onChange={handleAddressChange}
                                    />
                                </div>
                                <div className="form-row">
                                    <input
                                        type="text"
                                        name="country"
                                        placeholder="Quốc gia"
                                        value={userAddress?.country || ''}
                                        onChange={handleAddressChange}
                                    />
                                </div>
                            </div>
                            
                            <div className="payment-methods">
                                <h3>Phương thức thanh toán</h3>
                                <div className="payment-options">
                                    <label>
                                        <input
                                            type="radio"
                                            name="paymentMethod"
                                            value="CASH"
                                            checked={paymentMethod === 'CASH'}
                                            onChange={() => setPaymentMethod('CASH')}
                                        />
                                        Thanh toán khi nhận hàng
                                    </label>
                                    <label>
                                        <input
                                            type="radio"
                                            name="paymentMethod"
                                            value="VNPAY"
                                            checked={paymentMethod === 'VNPAY'}
                                            onChange={() => setPaymentMethod('VNPAY')}
                                        />
                                        Thanh toán qua VNPAY
                                    </label>
                                </div>
                            </div>
                            
                            <div className="checkout-actions">
                                <button className="back-button" onClick={() => setShowCheckoutForm(false)}>Quay lại</button>
                                <button className="confirm-button" onClick={handleCheckout}>Xác nhận đặt hàng</button>
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}

export default CartPage;
