import React, {useState} from "react";
import '../../style/navbar.css';
import { NavLink, useNavigate } from "react-router-dom";
import ApiService from "../../service/ApiService";

const Navbar = () =>{
    const [searchValue, setSearchValue] = useState("");
    const [isSearching, setIsSearching] = useState(false);
    const navigate = useNavigate();

    const isAdmin = ApiService.isAdmin();
    const isAuthenticated = ApiService.isAuthenticated();

    const handleSearchChange =(e) => {
        setSearchValue(e.target.value);
    }

    const handleSearchSubmit = async (e) => {
        e.preventDefault();
        if (!searchValue.trim()) return;

        setIsSearching(true);
        try {
            const result = await ApiService.smartSearch(searchValue);
            localStorage.setItem('lastSmartSearch', JSON.stringify({
                searchResult: result,
                searchQuery: searchValue
            }));
            
            // Log để debug
            console.log('Search result:', result);
            
            navigate('/', { 
                state: { 
                    searchResult: result,
                    searchQuery: searchValue 
                } 
            });
        } catch (error) {
            console.error('Search error:', error);
            alert('Có lỗi xảy ra khi tìm kiếm. Vui lòng thử lại.');
        } finally {
            setIsSearching(false);
        }
    }

    const handleLogout = () => {
        const confirm = window.confirm("Are you sure you want to logout? ");
        if(confirm){
            ApiService.logout();
            setTimeout(()=>{
                navigate('/login')
            }, 500);
        }
    }

    return(
        <nav className="navbar">
            <div className="navbar-top">
                <div className="navbar-brand">
                    <NavLink to="/">
                        <img src="/logo192.png" alt="Logo" className="navbar-logo" />
                    </NavLink>
                </div>
                <div className="navbar-icons">
                    <NavLink to="/cart" className="navbar-icon">
                        <svg width="28" height="28" fill="none" stroke="currentColor" strokeWidth="2">
                            <circle cx="9" cy="21" r="1"/>
                            <circle cx="20" cy="21" r="1"/>
                            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
                        </svg>
                    </NavLink>
                    <div className="navbar-user">
                        <span className="navbar-icon">
                            <svg width="28" height="28" fill="none" stroke="currentColor" strokeWidth="2">
                                <circle cx="14" cy="10" r="5"/>
                                <path d="M3 25c0-5 9-7 11-7s11 2 11 7"/>
                            </svg>
                        </span>
                        <div className="user-menu">
                            {isAuthenticated ? (
                                <>
                                    <button className="user-menu-btn" onClick={() => navigate('/profile')}>Thông tin tài khoản</button>
                                    <button className="user-menu-btn" onClick={() => navigate('/order-history')}>Lịch sử mua hàng</button>
                                    <button className="user-menu-btn" onClick={handleLogout}>Đăng xuất</button>
                                </>
                            ) : (
                                <button className="user-menu-btn" onClick={() => navigate('/login')}>Đăng nhập</button>
                            )}
                        </div>
                    </div>
                </div>
            </div>
            <div className="navbar-bottom">
                <form className="navbar-search" onSubmit={handleSearchSubmit}>
                    <input 
                        type="text" 
                        placeholder="Tìm kiếm thông minh (ví dụ: điện thoại Samsung dưới 10 triệu còn hàng)" 
                        value={searchValue}
                        onChange={handleSearchChange}
                        className={isSearching ? 'searching' : ''}
                    />
                    <button type="submit" disabled={isSearching}>
                        {isSearching ? 'Đang tìm...' : 'Tìm kiếm'}
                    </button>
                </form>
                <div className="navbar-links">
                    <NavLink to="/" className="navbar-link">Home</NavLink>
                    <NavLink to="/categories" className="navbar-link">Categories</NavLink>
                    {isAdmin && <NavLink to="/admin" className="navbar-link">Admin</NavLink>}
                </div>
            </div>
        </nav>
    );
};

export default Navbar;