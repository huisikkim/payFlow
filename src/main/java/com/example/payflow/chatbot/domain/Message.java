package com.example.payflow.chatbot.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageRole role;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column
    private Intent intent;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Message(MessageRole role, String content, Intent intent) {
        this.role = role;
        this.content = content;
        this.intent = intent;
        this.createdAt = LocalDateTime.now();
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }
}
