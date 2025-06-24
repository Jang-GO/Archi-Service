package com.archiservice.chatbot.redis;

import io.lettuce.core.RedisCommandExecutionException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ReadOffset;

@Configuration
public class RedisStreamConfig {

  private static final Logger log = LoggerFactory.getLogger(RedisStreamConfig.class);

  @Autowired
  private RedisConnectionFactory connectionFactory;

  @PostConstruct
  public void initializeStreams() {
    log.info("Initializing Redis Stream Consumer Groups...");

    createConsumerGroup("ai-response-stream", "response-handler");

    createConsumerGroup("ai-request-stream", "request-processor");

    createConsumerGroup("image-request-stream", "image-request-processor"); // 스프링 -> AI (ai 서버의 컨슈머가 받음)

    createConsumerGroup("image-response-stream", "image-response-handler"); // AI -> 스프링 (스프링 컨슈머가 받음)


    log.info("Redis Stream Consumer Groups initialization completed");
  }

  private void createConsumerGroup(String streamName, String groupName) {
    RedisConnection conn = null;
    try {
      conn = connectionFactory.getConnection();
      conn.streamCommands().xGroupCreate(
          streamName.getBytes(),
          groupName,
          ReadOffset.latest(),
          true
      );
      log.info("Consumer Group '{}' created for stream '{}'", groupName, streamName);

    } catch (RedisSystemException | RedisCommandExecutionException e) {
      log.warn("Consumer Group '{}' for stream '{}' already exists or error: {}",
          groupName, streamName, e.getMessage());
    } catch (Exception e) {
      log.error("Failed to create Consumer Group '{}' for stream '{}': {}",
          groupName, streamName, e.getMessage());
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (Exception e) {
          log.warn("Failed to close Redis connection: {}", e.getMessage());
        }
      }
    }
  }
}