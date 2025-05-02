import './App.css';
import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { ProtectedRoute, AdminRoute } from './service/Guard';
import Navbar from './component/common/Navbar';
import Footer from './component/common/footer';
import { CartProvider } from './component/context/CartContext';
import { OrderProvider } from './component/context/OrderContext';
import Home from './component/pages/Home';
import ProductDetailsPage from './component/pages/ProductDetailsPage';
import CategoryListPage from './component/pages/CategoryListPage';
import CategoryProductsPage from './component/pages/CategoryProductsPage';
import CartPage from './component/pages/CartPage';
import RegisterPage from './component/pages/RegisterPage';
import LoginPage from './component/pages/LoginPage';
import ProfilePage from './component/pages/ProfilePage';
import AddressPage from './component/pages/AddressPage';
import AdminPage from './component/admin/AdminPage';
import AdminCategoryPage from './component/admin/AdminCategoryPage';
import AddCategory from './component/admin/AddCategory';
import EditCategory from './component/admin/EditCategory';
import AdminProductPage from './component/admin/AdminProductPage';
import AddProductPage from './component/admin/AddProductPage';
import EditProductPage from './component/admin/EditProductPage';
import AdminOrdersPage from './component/admin/AdminOrderPage';
import AdminOrderDetailsPage from './component/admin/AdminOrderDetailsPage';
import ChatBot from './component/common/ChatBot';
import OrderCallbackPage from './component/pages/OrderCallbackPage';
import OrderHistoryPage from './component/pages/OrderHistoryPage';
import OrderDetailsPage from './component/pages/OrderDetailsPage';
import { toast } from 'react-hot-toast';
import ApiService from './service/ApiService';
import { AppProvider } from './context/AppContext';
import { Toaster } from 'react-hot-toast';

// Tạo component riêng để xử lý navigation
function AppContent() {
  const navigate = useNavigate(); // Chỉ sử dụng trong component con
  
  useEffect(() => {
    // Kiểm tra nếu có đơn hàng chờ thanh toán khi người dùng quay lại ứng dụng
    const checkPendingPayment = async () => {
      const pendingOrderId = localStorage.getItem('pendingOrderId');
      const paymentMethod = localStorage.getItem('paymentMethod');
      const redirectToZaloPay = localStorage.getItem('redirectToZaloPay');
      const orderCreationTime = localStorage.getItem('orderCreationTime');
      
      // Nếu có đơn hàng chờ và người dùng đã được chuyển hướng đến ZaloPay
      if (pendingOrderId && redirectToZaloPay === 'true' && paymentMethod === 'ZALOPAY') {
        // Kiểm tra thời gian - chỉ kiểm tra nếu đơn hàng được tạo trong vòng 24 giờ qua
        const creationTime = new Date(orderCreationTime);
        const now = new Date();
        const hoursSinceCreation = (now - creationTime) / (1000 * 60 * 60);
        
        if (hoursSinceCreation < 24) {
          try {
            // Kiểm tra trạng thái thanh toán
            const result = await ApiService.getOrderStatus(pendingOrderId);
            
            if (result.success) {
              if (result.paymentStatus === 'PAID') {
                // Thanh toán thành công - thông báo và chuyển hướng
                toast.success('Đơn hàng của bạn đã được thanh toán thành công!');
                navigate(`/order-details/${pendingOrderId}`);
                
                // Xóa thông tin lưu trữ
                localStorage.removeItem('pendingOrderId');
                localStorage.removeItem('paymentMethod');
                localStorage.removeItem('redirectToZaloPay');
                localStorage.removeItem('orderCreationTime');
                localStorage.removeItem('app_trans_id');
              }
              // Nếu chưa thanh toán hoặc thất bại, không làm gì
            }
          } catch (error) {
            console.error('Error checking pending payment:', error);
          }
        } else {
          // Đơn hàng quá cũ, xóa thông tin lưu trữ
          localStorage.removeItem('pendingOrderId');
          localStorage.removeItem('paymentMethod');
          localStorage.removeItem('redirectToZaloPay');
          localStorage.removeItem('orderCreationTime');
          localStorage.removeItem('app_trans_id');
        }
      }
    };
    
    checkPendingPayment();
  }, [navigate]);
  
  return (
    <>
      <Toaster position="top-right" />
      <Routes>
        {/* OUR ROUTES */}
        <Route exact path='/' element={<Home/>}/>
        <Route path='/product/:productId' element={<ProductDetailsPage/>} />
        <Route path='/categories' element={<CategoryListPage/>}/>
        <Route path='/category/:categoryId' element={<CategoryProductsPage/>} />
        <Route path='/cart' element={<CartPage/>}/>
        <Route path='/register' element={<RegisterPage/>}/>
        <Route path='/login' element={<LoginPage/>}/>

        <Route path='/profile' element={<ProtectedRoute element={<ProfilePage/>} />} />
        <Route path='/add-address' element={<ProtectedRoute element={<AddressPage/>} />} />
        <Route path='/edit-address' element={<ProtectedRoute element={<AddressPage/>} />} />


        <Route path='/admin' element={<AdminRoute element={<AdminPage/>} />} />
        <Route path='/admin/categories' element={<AdminRoute element={<AdminCategoryPage/>} />} />
        <Route path='/admin/add-category' element={<AdminRoute element={<AddCategory/>} />} />
        <Route path='/admin/edit-category/:categoryId' element={<AdminRoute element={<EditCategory/>} />} />
        <Route path='/admin/products' element={<AdminRoute element={<AdminProductPage/>} />} />
        <Route path='/admin/add-product' element={<AdminRoute element={<AddProductPage/>} />} />
        <Route path='/admin/edit-product/:productId' element={<AdminRoute element={<EditProductPage/>} />} />

        <Route path='/admin/orders' element={<AdminOrdersPage />} />
        <Route path='/admin/order-details/:itemId' element={<AdminOrderDetailsPage />} />

        <Route path="/orders" element={<OrderHistoryPage />} />
        <Route path="/order-details/:orderId" element={<OrderDetailsPage />} />
        <Route path="/payment-callback" element={<OrderCallbackPage />} />
      </Routes>
      <ChatBot />
      <Footer/>
    </>
  );
}

function App() {
  return (
    <AppProvider>
      <CartProvider>
        <OrderProvider>
          <Router>
            <Navbar/>
            <AppContent />
            <Footer/>
          </Router>
        </OrderProvider>
      </CartProvider>
    </AppProvider>
  );
}

export default App;
