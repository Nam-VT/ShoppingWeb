import React, { useState } from 'react';
import './Navbar.css';
import {NavLink, useNavigate} from 'react-router-dom';
import ApiService from '../../ApiService';

const Navbar = () => {
    const [searchValue, setSearchValue] = useState('');
    const navigate = useNavigate();
    
    const idAmin = ApiService.isAdmin();
    const isAuthenticated = ApiService.isAuthenticated();

    const handleSearchChange = (e) => {
        setSearchValue(e.target.value);
    };

    const handleSearchSubmit = async (e) => {
        e.preventDefault();
        
        navigate(`/?search=${searchValue}`);
    };

    const handleLogout = () => {
        const confirmLogout = window.confirm('Are you sure you want to logout?');
        if (confirmLogout) {
            ApiService.logout();
            navigate('/login');
        }
    };
    
    return (
        <nav className="navbar">
            <div className="navbar-brand">
                <NavLink to="/"><img src="" alt = "logo" /></NavLink>
            </div>
            <form 
        </nav>
    )
}
