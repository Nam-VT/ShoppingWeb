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
            <div className="navbar-brand">
                <NavLink to="/" ></NavLink>
            </div>
            {/* SEARCH FORM - Nâng cấp UI */}
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

            <div className="navbar-link">
                <NavLink to="/" >Home</NavLink>
                <NavLink to="/categories" >Categories</NavLink>
                {isAuthenticated && <NavLink to="/profile" >My Account</NavLink>}
                {isAdmin && <NavLink to="/admin" >Admin</NavLink>}
                {!isAuthenticated && <NavLink to="/login" >Login</NavLink>}
                {isAuthenticated &&<NavLink onClick={handleLogout} >Logout</NavLink>}
                <NavLink to="/cart">Cart</NavLink>
            </div>
        </nav>
    );
};

export default Navbar;