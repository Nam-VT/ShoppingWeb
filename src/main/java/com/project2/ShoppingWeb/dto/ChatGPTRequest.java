package com.project2.ShoppingWeb.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ChatGPTRequest {
    private String model;
    private List<Message> messages;

    public ChatGPTRequest(String model, String prompt) {
        this.model = model;
        this.messages = new ArrayList<>();
        this.messages.add(new Message("user", prompt));
    }
}

@Data
class Message {
    private String role;
    private String content;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }
} 