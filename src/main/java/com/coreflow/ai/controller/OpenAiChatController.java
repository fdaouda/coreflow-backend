package com.coreflow.ai.controller;

import com.coreflow.config.ai.OrderAiToolsConfig;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:4200")
public class OpenAiChatController {

    private final ChatClient chatClient;
    private final OrderAiToolsConfig orderAiToolsConfig; // 1. Injection du composant d'outils

    public OpenAiChatController(ChatClient.Builder chatBuilder, OrderAiToolsConfig orderAiToolsConfig) {
        this.chatClient = chatBuilder.build();
        this.orderAiToolsConfig = orderAiToolsConfig;
    }

    @PostMapping("/chat")
    public String askAgent(@RequestBody String userPrompt) {
        return chatClient.prompt()
                .system("Tu es l'assistant virtuel intelligent de CoreFlow. Tu réponds de manière courtoise et précise en utilisant les outils à ta disposition.")
                .user(userPrompt)
                .tools(orderAiToolsConfig)
                .call()
                .content();
    }

}
