package com.archiservice.chatbot.redis;

import com.archiservice.chatbot.dto.ChatMessageDto;
import com.archiservice.chatbot.service.impl.TendencyImageService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * Redis Stream에서 메시지를 수신하여 처리.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiImageResponseConsumer {

  private final TendencyImageService tendencyImageService;
  private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamContainer;
  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  @PostConstruct
  public void startListening() {
    streamContainer.receive(
      Consumer.from("image-response-handler", "image-consumer-1"),
        StreamOffset.create("image-response-stream", ReadOffset.lastConsumed()),
        this::handleMessage
    );
    streamContainer.start();
  }

  private void handleMessage(MapRecord<String, String, String> entries) {
    // 메시지 한방에 온다

    /**
     {
     "user_id": 123,
     "summary": "사진을 분석한 결과...",
     "tags": ["감성적", "계획적", "소극적"],
     "message_type": "TENDENCY_SUMMARY"
     }
     */
  }
}
