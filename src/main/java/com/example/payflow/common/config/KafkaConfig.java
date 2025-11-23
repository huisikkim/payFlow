package com.example.payflow.common.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    
    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name("OrderCreated")
            .partitions(1)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic ingredientOrderCreatedTopic() {
        return TopicBuilder.name("IngredientOrderCreated")
            .partitions(1)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic ingredientOrderConfirmedTopic() {
        return TopicBuilder.name("IngredientOrderConfirmed")
            .partitions(1)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic ingredientOrderRejectedTopic() {
        return TopicBuilder.name("IngredientOrderRejected")
            .partitions(1)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic invoiceUploadedTopic() {
        return TopicBuilder.name("InvoiceUploaded")
            .partitions(1)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic invoiceParsedTopic() {
        return TopicBuilder.name("InvoiceParsed")
            .partitions(1)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic settlementCreatedTopic() {
        return TopicBuilder.name("SettlementCreated")
            .partitions(1)
            .replicas(1)
            .build();
    }
    
    @Bean
    public NewTopic settlementCompletedTopic() {
        return TopicBuilder.name("SettlementCompleted")
            .partitions(1)
            .replicas(1)
            .build();
    }
}
