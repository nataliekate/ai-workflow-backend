package com.example.aiworkflowbackend.llm;

import com.example.aiworkflowbackend.dto.WorkflowDtos.ModelProviderDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A factory component responsible for providing ChatClient instances
 * based on a requested model ID. It discovers available ChatModel beans
 * and maps them to simple identifiers.
 */

@Component
public class ChatModelFactory {
    private final Map<String, ChatClient> chatClients = new ConcurrentHashMap<>();
    private final Map<String, String> modelDescriptions = new ConcurrentHashMap<>();

    /**
     * Injects all available ChatModel beans from the Spring context.
     * It then creates a ChatClient for each and maps them by a simple ID.
     * @param chatModels The list of ChatModel beans discovered by Spring.
     */
    public ChatModelFactory(List<ChatModel> chatModels) {
        for (ChatModel model : chatModels) {
            if (model instanceof OpenAiChatModel) {
                String id = "openai";
                this.chatClients.put(id, ChatClient.create(model));
                this.modelDescriptions.put(id, "OpenAI Model (e.g., GPT-4o, GPT-3.5-Turbo)");
            }
            // Add more 'else if' blocks here to support other models like Anthropic, Gemini, etc.
        }
    }

    /**
     * Retrieves a ChatClient for the given model ID.
     * @param modelId The identifier for the model (e.g., "openai", "ollama").
     * @return An Optional containing the ChatClient if found, otherwise empty.
     */
    public Optional<ChatClient> getChatClient(String modelId) {
        return Optional.ofNullable(chatClients.get(modelId));
    }

    /**
     * Returns a list of all available model providers.
     * @return A list of ModelProviderDto objects for the UI to consume.
     */
    public List<ModelProviderDto> getAvailableModels() {
        return chatClients.keySet().stream()
                .map(id -> new ModelProviderDto(id, modelDescriptions.getOrDefault(id, "N/A")))
                .collect(Collectors.toList());
    }
}
