package com.example.payflow.chatbot.application;

import com.example.payflow.chatbot.domain.Intent;
import org.springframework.stereotype.Service;

@Service
public class IntentMatcher {

    public Intent detectIntent(String message) {
        if (message == null || message.trim().isEmpty()) {
            return Intent.UNKNOWN;
        }
        return Intent.detectIntent(message);
    }
}
