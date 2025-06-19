package com.archiservice.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AiConfig{
    @Bean
    @Qualifier("bannerClient")
    ChatClient bannerClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
