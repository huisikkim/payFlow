package com.example.payflow.chatbot.presentation;

import com.example.payflow.chatbot.application.ChatbotService;
import com.example.payflow.chatbot.application.dto.ChatRequest;
import com.example.payflow.chatbot.application.dto.ChatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat request from user: {}", request.getUserId());
        ChatResponse response = chatbotService.chat(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/conversations/{conversationId}/history")
    public ResponseEntity<List<ChatResponse>> getHistory(
        @PathVariable Long conversationId,
        @RequestParam String userId
    ) {
        log.info("Fetching conversation history: {}", conversationId);
        List<ChatResponse> history = chatbotService.getConversationHistory(userId, conversationId);
        return ResponseEntity.ok(history);
    }

    @PostMapping("/conversations/{conversationId}/close")
    public ResponseEntity<Void> closeConversation(
        @PathVariable Long conversationId,
        @RequestParam String userId
    ) {
        log.info("Closing conversation: {}", conversationId);
        chatbotService.closeConversation(userId, conversationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chatbot service is running");
    }
}
