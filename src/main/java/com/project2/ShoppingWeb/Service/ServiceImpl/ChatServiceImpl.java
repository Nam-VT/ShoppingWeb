package com.project2.ShoppingWeb.Service.ServiceImpl;

import com.project2.ShoppingWeb.Service.ChatService;
import com.project2.ShoppingWeb.dto.ChatGPTRequest;
import com.project2.ShoppingWeb.dto.ChatGPTResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Service
public class ChatServiceImpl implements ChatService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getChatResponse(String message) {
        try {
            String url = "https://api.openai.com/v1/chat/completions";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            ChatGPTRequest request = new ChatGPTRequest("gpt-3.5-turbo", message);
            
            HttpEntity<ChatGPTRequest> entity = new HttpEntity<>(request, headers);
            
            ChatGPTResponse response = restTemplate.postForObject(
                url, 
                entity, 
                ChatGPTResponse.class
            );

            if (response != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
            
            return "No response generated.";
                
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I encountered an error. Please try again later.";
        }
    }
} 