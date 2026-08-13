package com.coreflow.ai.controller;

import com.coreflow.config.ai.OrderAiToolsConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Agent IA CoreFlow", description = "Endpoints pour l'interaction en langage naturel avec l'assistant IA et le Function Calling")
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

    @Operation(
            summary = "Interroger l'agent IA CoreFlow",
            description = "Envoie une question ou une instruction en langage naturel. L'agent IA utilise les outils métiers (Function Calling) pour interagir avec les commandes."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Réponse générée par l'agent IA avec succès",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"),
                            examples = @ExampleObject(value = "La commande #123 a été trouvée. Son statut est EXPEDIÉE."))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur interne lors de la communication avec le modèle IA ou l'exécution de l'outil",
                    content = @Content
            )
    })
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
