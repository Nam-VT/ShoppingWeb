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
    const [paymentMethod, setPaymentMethod] = useState('CASH');
    const [showCheckoutForm, setShowCheckoutForm] = useState(false);
    const [selectedItems, setSelectedItems] = useState({});
    const [paymentStatus, setPaymentStatus] = useState(null); // 'pending', 'success', 'failed'
    const [paymentPopupWindow, setPaymentPopupWindow] = useState(null);
    const navigate = useNavigate();

    // Khởi tạo selectedItems khi cart thay đổi
    useEffect(() => {
        const initialSelected = {};
        cart.forEach(item => {
            initialSelected[item.id] = true; // Mặc định chọn tất cả
        });
        setSelectedItems(initialSelected);
    }, [cart]);

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

    // Thêm effect để kiểm tra trạng thái thanh toán
    useEffect(() => {
        // Hàm kiểm tra trạng thái cửa sổ popup
        const checkPaymentWindow = () => {
            // Nếu popup đã mở và đã đóng
            if (paymentPopupWindow && paymentPopupWindow.closed) {
                const orderId = localStorage.getItem('pendingOrderId');
                
                if (orderId) {
                    setMessage('Đang kiểm tra trạng thái thanh toán...');
                    
                    // Kiểm tra trạng thái đơn hàng từ API
                    const checkOrderStatus = async () => {
                        try {
                            // Đợi một khoảng thời gian để backend có thể xử lý callback từ cổng thanh toán
                            setTimeout(async () => {
                                try {
                                    const orderStatus = await ApiService.getOrderStatus(orderId);
                                    
                                    if (orderStatus.paymentStatus === 'PAID' || orderStatus.status === 'PAID') {
                                        // Thanh toán thành công
                                        setPaymentStatus('success');
                                        setMessage('Thanh toán thành công! Cảm ơn bạn đã đặt hàng.');
                                        
                                        // Xóa các sản phẩm đã chọn khỏi giỏ hàng
                                        const selectedCartItems = cart.filter(item => selectedItems[item.id]);
                                        selectedCartItems.forEach(item => {
                                            dispatch({ type: 'REMOVE_ITEM', payload: item });
                                        });
                                        
                                        setShowCheckoutForm(false);
                                    } else if (orderStatus.paymentStatus === 'FAILED' || orderStatus.status === 'FAILED') {
                                        // Thanh toán thất bại
                                        setPaymentStatus('failed');
                                        setMessage('Thanh toán thất bại. Vui lòng thử lại hoặc chọn phương thức thanh toán khác.');
                                    } else {
                                        // Trạng thái không xác định
                                        setPaymentStatus('pending');
                                        setMessage('Không thể xác định trạng thái thanh toán. Vui lòng kiểm tra lại sau.');
                                    }
                                    
                                    // Xóa ID đơn hàng đang chờ
                                    localStorage.removeItem('pendingOrderId');
                                    localStorage.removeItem('zalopay_trans_id');
                                } catch (error) {
                                    console.error("Error checking order status:", error);
                                    setPaymentStatus('failed');
                                    setMessage('Lỗi khi kiểm tra trạng thái thanh toán. Vui lòng liên hệ hỗ trợ.');
                                }
                            }, 3000); // Đợi 3 giây để backend xử lý webhook
                        } catch (error) {
                            console.error("Error in delayed status check:", error);
                        }
                    };
                    
                    checkOrderStatus();
                } else {
                    // Không có ID đơn hàng
                    setPaymentStatus('failed');
                    setMessage('Không tìm thấy thông tin đơn hàng.');
                }
                
                // Xóa tham chiếu đến popup
                setPaymentPopupWindow(null);
            }
        };
        
        // Kiểm tra mỗi 1 giây
        const intervalId = setInterval(checkPaymentWindow, 1000);
        
        // Cleanup khi component unmount
        return () => clearInterval(intervalId);
    }, [dispatch, paymentPopupWindow, cart, selectedItems]);

    // Các hàm xử lý giỏ hàng
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

    // Xử lý chọn/bỏ chọn sản phẩm
    const toggleSelectItem = (itemId) => {
        setSelectedItems(prev => ({
            ...prev,
            [itemId]: !prev[itemId]
        }));
    }

    // Chọn/bỏ chọn tất cả sản phẩm
    const toggleSelectAll = () => {
        const allSelected = cart.every(item => selectedItems[item.id]);
        const newSelectedItems = {};
        
        cart.forEach(item => {
            newSelectedItems[item.id] = !allSelected;
        });
        
        setSelectedItems(newSelectedItems);
    }

    // Tính tổng tiền cho các sản phẩm được chọn
    const selectedTotalPrice = cart
        .filter(item => selectedItems[item.id])
        .reduce((total, item) => total + item.price * item.quantity, 0);

    // Kiểm tra xem có sản phẩm nào được chọn không
    const hasSelectedItems = cart.some(item => selectedItems[item.id]);

    // Bắt đầu thanh toán
    const initiateCheckout = () => {
        if (!hasSelectedItems) {
            setMessage("Vui lòng chọn ít nhất một sản phẩm để thanh toán");
            setTimeout(() => setMessage(null), 3000);
            return;
        }

        if (!ApiService.isAuthenticated()) {
            const confirmLogin = window.confirm(
                "Bạn cần đăng nhập để tiếp tục thanh toán. Đăng nhập ngay?"
            );
            
            if (confirmLogin) {
                navigate("/login");
            }
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

    // Xử lý thanh toán
    const handleCheckout = async () => {
        if (!userAddress || !userAddress.street || !userAddress.city) {
            setMessage("Vui lòng nhập đầy đủ thông tin địa chỉ");
            return;
        }

        if (!hasSelectedItems) {
            setMessage("Vui lòng chọn ít nhất một sản phẩm để thanh toán");
            return;
        }

        setPaymentStatus('processing');

        // Lọc các sản phẩm được chọn
        const selectedCartItems = cart.filter(item => selectedItems[item.id]);

        // Chuẩn bị dữ liệu đơn hàng
        const orderRequest = {
            totalPrice: selectedTotalPrice,
            paymentMethod,
            shippingAddress: `${userAddress.street}, ${userAddress.city}, ${userAddress.state || ''}, ${userAddress.country || ''}`,
            status: paymentMethod === 'CASH' ? 'PENDING' : 'PROCESSING',
            items: selectedCartItems.map(item => ({
                productId: item.id,
                quantity: item.quantity,
                price: item.price * item.quantity
            }))
        };

        try {
            // Lưu địa chỉ người dùng
            if (userAddress) {
                const addressToSave = {
                    id: userAddress.id,
                    street: userAddress.street,
                    city: userAddress.city,
                    state: userAddress.state || '',
                    country: userAddress.country || ''
                };
                
                await ApiService.saveAddress(addressToSave);
            }
            
            // Tạo đơn hàng
            console.log("Sending order request:", orderRequest);
            const createdOrder = await ApiService.createOrder(orderRequest);
            console.log("Order created:", createdOrder);
            
            if (createdOrder) {
                const orderId = createdOrder.id;
                
                if (paymentMethod === 'CASH') {
                    setPaymentStatus('success');
                    setMessage('Đặt hàng thành công!');
                    
                    // Xóa các sản phẩm đã chọn khỏi giỏ hàng
                    selectedCartItems.forEach(item => {
                        dispatch({ type: 'REMOVE_ITEM', payload: item });
                    });
                    
                    setShowCheckoutForm(false);
                } else if (paymentMethod === 'VNPAY') {
                    try {
                        const paymentUrl = await ApiService.createVNPayUrl(orderId, selectedTotalPrice);
                        
                        // Lưu ID đơn hàng vào localStorage để kiểm tra sau này
                        localStorage.setItem('pendingOrderId', orderId);
                        
                        // Mở popup thay vì chuyển hướng
                        const popupWindow = window.open(
                            paymentUrl,
                            'VNPayPayment',
                            'width=1000,height=800,left=200,top=100'
                        );
                        
                        setPaymentPopupWindow(popupWindow);
                        setMessage('Vui lòng hoàn tất thanh toán trong cửa sổ mới.');
                    } catch (error) {
                        console.error("VNPAY error:", error);
                        setPaymentStatus('failed');
                        setMessage(`Lỗi khi tạo thanh toán VNPAY: ${error.message || 'Không xác định'}`);
                    }
                } else if (paymentMethod === 'ZALOPAY') {
                    try {
                        const zaloPayResponse = await ApiService.createZaloPayUrl(orderId, selectedTotalPrice);
                        console.log("ZaloPay response:", zaloPayResponse);
                        
                        if (zaloPayResponse && zaloPayResponse.zalopay_response) {
                            let zaloPayData;
                            if (typeof zaloPayResponse.zalopay_response === 'string') {
                                zaloPayData = JSON.parse(zaloPayResponse.zalopay_response);
                            } else {
                                zaloPayData = zaloPayResponse.zalopay_response;
                            }
                            
                            if (zaloPayData.return_code === 1) {
                                // Lưu thông tin đơn hàng và app_trans_id
                                localStorage.setItem('pendingOrderId', orderId);
                                localStorage.setItem('zalopay_trans_id', zaloPayResponse.app_trans_id);
                                
                                // Mở popup thay vì chuyển hướng
                                const popupWindow = window.open(
                                    zaloPayData.order_url,
                                    'ZaloPayPayment',
                                    'width=1000,height=800,left=200,top=100'
                                );
                                
                                setPaymentPopupWindow(popupWindow);
                                setMessage('Vui lòng hoàn tất thanh toán trong cửa sổ mới.');
                            } else {
                                setPaymentStatus('failed');
                                setMessage(`Lỗi ZaloPay: ${zaloPayData.return_message || 'Không thể tạo thanh toán'}`);
                            }
                        } else {
                            setPaymentStatus('failed');
                            setMessage('Không nhận được phản hồi hợp lệ từ ZaloPay');
                        }
                    } catch (error) {
                        console.error("ZaloPay error:", error);
                        setPaymentStatus('failed');
                        setMessage(`Lỗi khi kết nối ZaloPay: ${error.message || 'Lỗi không xác định'}`);
                    }
                }
            } else {
                setPaymentStatus('failed');
                setMessage('Không thể tạo đơn hàng, vui lòng thử lại');
            }
        } catch (error) {
            console.error("Checkout error:", error);
            setPaymentStatus('failed');
            setMessage(error.response?.data?.message || error.message || 'Không thể tạo đơn hàng');
        }
    };

    return (
        <div className="cart-page">
            <h1>Giỏ hàng</h1>
            {message && (
                <div className={`response-message ${paymentStatus ? `payment-${paymentStatus}` : ''}`}>
                    {message}
                </div>
            )}

            {paymentStatus === 'processing' && (
                <div className="payment-processing-overlay">
                    <div className="processing-content">
                        <div className="spinner"></div>
                        <p>Đang xử lý thanh toán...</p>
                        <p>Vui lòng hoàn tất thanh toán trong cửa sổ mới.</p>
                    </div>
                </div>
            )}

            {cart.length === 0 ? (
                <p>Giỏ hàng của bạn đang trống</p>
            ) : (
                <div>
                    {!showCheckoutForm ? (
                        <>
                            <div className="select-all-container">
                                <label>
                                    <input
                                        type="checkbox"
                                        checked={cart.length > 0 && cart.every(item => selectedItems[item.id])}
                                        onChange={toggleSelectAll}
                                    />
                                    Chọn tất cả
                                </label>
                            </div>
                            
                            <ul>
                                {cart.map(item => (
                                    <li key={item.id}>
                                        <div className="item-select">
                                            <input
                                                type="checkbox"
                                                checked={selectedItems[item.id] || false}
                                                onChange={() => toggleSelectItem(item.id)}
                                            />
                                        </div>
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
                                                <button onClick={() => decrementItem(item)}>-</button>
                                                <span>{item.quantity}</span>
                                                <button onClick={() => incrementItem(item)}>+</button>
                                            </div>
                                            <span>${item.price.toFixed(2)}</span>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                            <h2>Tổng cộng (sản phẩm đã chọn): ${selectedTotalPrice.toFixed(2)}</h2>
                            <button 
                                className="checkout-button" 
                                onClick={initiateCheckout}
                                disabled={!hasSelectedItems}
                            >
                                Thanh toán
                            </button>
                        </>
                    ) : (
                        <div className="checkout-form">
                            <h2>Thông tin đặt hàng</h2>
                            
                            <div className="order-summary">
                                <h3>Tóm tắt đơn hàng</h3>
                                <ul>
                                    {cart.filter(item => selectedItems[item.id]).map(item => (
                                        <li key={item.id}>
                                            <span>{item.name} x {item.quantity}</span>
                                            <span>${(item.price * item.quantity).toFixed(2)}</span>
                                        </li>
                                    ))}
                                </ul>
                                <div className="total">
                                    <strong>Tổng cộng:</strong>
                                    <strong>${selectedTotalPrice.toFixed(2)}</strong>
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
                                    <label>
                                        <input
                                            type="radio"
                                            name="paymentMethod"
                                            value="ZALOPAY"
                                            checked={paymentMethod === 'ZALOPAY'}
                                            onChange={() => setPaymentMethod('ZALOPAY')}
                                        />
                                        Thanh toán qua ZaloPay
                                    </label>
                                </div>
                            </div>
                            
                            <div className="checkout-actions">
                                <button className="back-button" onClick={() => setShowCheckoutForm(false)}>Quay lại</button>
                                <button 
                                    className="confirm-button" 
                                    onClick={handleCheckout}
                                    disabled={paymentStatus === 'processing'}
                                >
                                    Xác nhận đặt hàng
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}

export default CartPage;
