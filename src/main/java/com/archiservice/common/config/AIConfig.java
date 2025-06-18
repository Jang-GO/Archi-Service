package com.archiservice.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class AIConfig {
    @Bean
    ChatClient reviewCleanBot(ChatClient.Builder builder) {
        return builder
                .defaultSystem(new ClassPathResource("prompts/moderation-rule-prompt.txt"))
                .build();
    }
}
