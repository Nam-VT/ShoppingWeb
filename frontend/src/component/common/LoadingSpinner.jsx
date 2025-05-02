import React from 'react';
import '../../style/loadingSpinner.css';

const LoadingSpinner = ({ text = 'Đang tải...' }) => {
    return (
        <div className="loading-container">
            <div className="loading-spinner"></div>
            <p className="loading-text">{text}</p>
        </div>
    );
};

export default LoadingSpinner; 