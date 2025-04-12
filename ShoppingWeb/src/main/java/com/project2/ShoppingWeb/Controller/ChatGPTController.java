package com.project2.ShoppingWeb.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.project2.ShoppingWeb.DTO.ChatGPTRequest;
import com.project2.ShoppingWeb.DTO.ChatGPTResponse;

@RestController
@RequestMapping("/bot")
public class ChatGPTController {

    @Value("${openai.model}")
    private String model;

    @Value(("${openai.api.url}"))
    private String apiURL;

    @Autowired
    private RestTemplate template;

    @GetMapping("/chat")
    public String chat(@RequestParam("prompt") String prompt){
        ChatGPTRequest request=new ChatGPTRequest(model, prompt);
        ChatGPTResponse chatGptResponse = template.postForObject(apiURL, request, ChatGPTResponse.class);
		if (chatGptResponse == null || chatGptResponse.getChoices() == null || chatGptResponse.getChoices().isEmpty()) {
			return "No response from ChatGPT";
		}
        return chatGptResponse.getChoices().get(0).getMessage().getContent();
    }
}