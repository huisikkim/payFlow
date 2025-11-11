package com.example.payflow.sessionreplay.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Session Replay Kafka 토픽 설정
 */
@Configuration
public class SessionReplayKafkaConfig {
    
    @Bean
    public NewTopic sessionInteractionEventTopic() {
        return TopicBuilder.name("SessionInteractionEvent")
            .partitions(3)  // 병렬 처리를 위한 파티션
            .replicas(1)
            .config(TopicConfig.RETENTION_MS_CONFIG, "2592000000") // 30일 보관
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "gzip")
            .build();
    }
}
