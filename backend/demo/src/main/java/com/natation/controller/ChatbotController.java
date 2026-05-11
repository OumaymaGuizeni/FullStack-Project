package com.natation.controller;

import com.natation.dto.ChatbotRequest;
import com.natation.dto.ChatbotResponse;
import com.natation.service.ChatbotService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @PostMapping("/ask")
    public ChatbotResponse ask(@RequestBody ChatbotRequest request) {
        return new ChatbotResponse(chatbotService.answer(request.getQuestion()));
    }
}
