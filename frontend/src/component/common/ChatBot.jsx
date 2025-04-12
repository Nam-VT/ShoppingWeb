import React, { useState } from 'react';
import ApiService from '../../service/ApiService';
import '../../style/chatbot.css';

const ChatBot = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [message, setMessage] = useState('');
    const [chatHistory, setChatHistory] = useState([]);
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!message.trim()) return;

        try {
            // Hiển thị tin nhắn của user
            setChatHistory(prev => [...prev, {
                type: 'user',
                content: message
            }]);
            
            // Bật trạng thái loading
            setIsLoading(true);
            
            // Gọi API để lấy phản hồi từ bot
            const response = await ApiService.chatWithBot(message);
            
            // Hiển thị phản hồi từ bot
            setChatHistory(prev => [...prev, {
                type: 'bot',
                content: response
            }]);

        } catch (error) {
            console.error('Lỗi:', error);
            // Hiển thị thông báo lỗi
            setChatHistory(prev => [...prev, {
                type: 'bot',
                content: 'Xin lỗi, tôi đang gặp sự cố. Vui lòng thử lại sau.'
            }]);
        } finally {
            // Tắt trạng thái loading và xóa tin nhắn
            setIsLoading(false);
            setMessage('');
        }
    };

    return (
        <div className="chatbot-container">
            {/* Chat bot icon */}
            <button 
                className={`chatbot-toggle ${isOpen ? 'open' : ''}`}
                onClick={() => setIsOpen(!isOpen)}
            >
                {isOpen ? '×' : '💬'}
            </button>

            {/* Chat window */}
            {isOpen && (
                <div className="chatbot-window">
                    <div className="chatbot-header">
                        <h3>Chat Bot</h3>
                    </div>
                    
                    <div className="chatbot-messages">
                        {chatHistory.map((chat, index) => (
                            <div key={index} className={`message ${chat.type}`}>
                                {chat.content}
                            </div>
                        ))}
                        {isLoading && (
                            <div className="message bot loading">
                                Đang nhập...
                            </div>
                        )}
                    </div>

                    <form onSubmit={handleSubmit} className="chatbot-input">
                        <input
                            type="text"
                            value={message}
                            onChange={(e) => setMessage(e.target.value)}
                            placeholder="Nhập câu hỏi của bạn..."
                            disabled={isLoading}
                        />
                        <button type="submit" disabled={isLoading}>
                            {isLoading ? 'Đang gửi...' : 'Gửi'}
                        </button>
                    </form>
                </div>
            )}
        </div>
    );
};

export default ChatBot;
