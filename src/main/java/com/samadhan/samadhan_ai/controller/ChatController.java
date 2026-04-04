package com.samadhan.samadhan_ai.controller;

import com.samadhan.samadhan_ai.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ChatController {

    @Autowired
    private AiService aiService;

    @PostMapping("/ask")
    public Map<String, String> ask(@RequestBody Map<String, String> request) {
        String query = request.getOrDefault("query", "");
        String domain = request.getOrDefault("domain", "general");
        String language = request.getOrDefault("language", "hi");

        String response = aiService.askQuestion(query, domain, language);

        return Map.of("response", response);
    }

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("status", "Server is running!");
    }
}