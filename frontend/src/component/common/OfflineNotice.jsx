import React, { useEffect, useState } from 'react';
import '../../style/offlineNotice.css';

const OfflineNotice = () => {
    const [isOnline, setIsOnline] = useState(navigator.onLine);

    useEffect(() => {
        const handleOnline = () => setIsOnline(true);
        const handleOffline = () => setIsOnline(false);

        window.addEventListener('online', handleOnline);
        window.addEventListener('offline', handleOffline);

        return () => {
            window.removeEventListener('online', handleOnline);
            window.removeEventListener('offline', handleOffline);
        };
    }, []);

    if (isOnline) {
        return null;
    }

    return (
        <div className="offline-notice">
            <span>Bạn đang offline. Một số tính năng có thể không hoạt động.</span>
        </div>
    );
};

export default OfflineNotice; 