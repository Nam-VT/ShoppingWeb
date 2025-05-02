import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import ApiService from '../../service/ApiService';
import { toast, Toaster } from 'react-hot-toast';
import '../../style/paymentCallback.css';

function OrderCallbackPage() {
    const navigate = useNavigate();
    const [status, setStatus] = useState('processing');
    const [message, setMessage] = useState('Đang xử lý thanh toán...');
    const [attempts, setAttempts] = useState(0);
    const [orderId, setOrderId] = useState(null);

    useEffect(() => {
        // Lấy orderId từ localStorage
        const pendingOrderId = localStorage.getItem('pendingOrderId');
        
        if (!pendingOrderId) {
            setStatus('error');
            setMessage('Không tìm thấy thông tin đơn hàng');
            return;
        }
        
        setOrderId(pendingOrderId);
        
        // Thiết lập interval để kiểm tra trạng thái thanh toán
        const checkInterval = setInterval(() => {
            checkPaymentStatus(pendingOrderId);
        }, 3000); // Kiểm tra mỗi 3 giây
        
        // Thiết lập timeout để dừng kiểm tra sau 2 phút
        const timeout = setTimeout(() => {
            clearInterval(checkInterval);
            if (status === 'processing') {
                setStatus('timeout');
                setMessage('Thời gian kiểm tra đã hết. Vui lòng kiểm tra trong lịch sử đơn hàng của bạn.');
            }
        }, 2 * 60 * 1000); // 2 phút
        
        // Cleanup
        return () => {
            clearInterval(checkInterval);
            clearTimeout(timeout);
        };
    }, []);
    
    const checkPaymentStatus = async (orderId) => {
        try {
            setAttempts(prev => prev + 1);
            
            // Nếu đã kiểm tra quá 40 lần (2 phút), dừng lại
            if (attempts > 40) {
                setStatus('timeout');
                setMessage('Thời gian kiểm tra đã hết. Vui lòng kiểm tra trong lịch sử đơn hàng của bạn.');
                return;
            }
            
            const result = await ApiService.getOrderStatus(orderId);
            
            if (result.success) {
                console.log(`Payment status checked (${attempts}):`, result.paymentStatus);
                
                if (result.paymentStatus === 'PAID') {
                    setStatus('success');
                    setMessage('Thanh toán thành công! Đơn hàng của bạn đã được xác nhận.');
                    
                    // Xóa thông tin đơn hàng chờ từ localStorage
                    localStorage.removeItem('pendingOrderId');
                    localStorage.removeItem('app_trans_id');
                    localStorage.removeItem('paymentMethod');
                    
                    // Hiển thị thông báo thành công
                    toast.success('Thanh toán thành công!');
                    
                    // Chuyển hướng sau 3 giây
                    setTimeout(() => {
                        navigate(`/order-details/${orderId}`);
                    }, 3000);
                    
                } else if (result.paymentStatus === 'FAILED') {
                    setStatus('error');
                    setMessage('Thanh toán thất bại! Vui lòng thử lại hoặc chọn phương thức thanh toán khác.');
                    
                    // Xóa thông tin đơn hàng chờ
                    localStorage.removeItem('pendingOrderId');
                    localStorage.removeItem('app_trans_id');
                    
                    // Hiển thị thông báo thất bại
                    toast.error('Thanh toán thất bại!');
                }
                // Nếu là PENDING hoặc PROCESSING, tiếp tục kiểm tra ở lần tiếp theo
            }
        } catch (error) {
            console.error('Error checking payment status:', error);
            // Không cập nhật UI ở đây để tiếp tục kiểm tra ở lần sau
        }
    };

    return (
        <div className="payment-callback-container">
            <Toaster position="top-right" />
            
            <div className={`payment-status ${status}`}>
                <h2>Thông tin thanh toán</h2>
                
                <div className="status-icon">
                    {status === 'processing' && <div className="loading-spinner"></div>}
                    {status === 'success' && <div className="success-icon">✓</div>}
                    {status === 'error' && <div className="error-icon">✗</div>}
                    {status === 'timeout' && <div className="timeout-icon">⏱</div>}
                </div>
                
                <p>{message}</p>
                
                {status === 'error' && (
                    <button onClick={() => navigate('/cart')}>
                        Quay lại giỏ hàng
                    </button>
                )}
                
                {status === 'timeout' && (
                    <div className="button-group">
                        <button onClick={() => navigate('/orders')}>
                            Xem lịch sử đơn hàng
                        </button>
                        <button onClick={() => navigate('/cart')}>
                            Quay lại giỏ hàng
                        </button>
                    </div>
                )}
                
                {status === 'success' && (
                    <p className="redirect-message">
                        Bạn sẽ được chuyển đến trang chi tiết đơn hàng sau vài giây...
                    </p>
                )}
                
                {orderId && (status === 'success' || status === 'timeout') && (
                    <button onClick={() => navigate(`/order-details/${orderId}`)}>
                        Xem chi tiết đơn hàng
                    </button>
                )}
            </div>
        </div>
    );
}

export default OrderCallbackPage; 