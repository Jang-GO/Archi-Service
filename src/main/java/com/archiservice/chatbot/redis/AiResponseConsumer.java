package com.archiservice.chatbot.redis;

import com.archiservice.chatbot.dto.response.ChatResponseDto;
import com.archiservice.chatbot.service.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AiResponseConsumer {

  private final AiService aiService;
  private final StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamContainer;
  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  @PostConstruct
  public void startListening() {
    streamContainer.receive(
        Consumer.from("response-handler", "consumer-1"),
        StreamOffset.create("ai-response-stream", ReadOffset.lastConsumed()),
        this::handleMessage
    );
    streamContainer.start();
  }

  private void handleMessage(MapRecord<String, String, String> record) {
    try {
      ChatResponseDto response = convertToDto(record.getValue());
      aiService.handleAIResponse(response);

      redisTemplate.opsForStream().acknowledge("response-handler", record);
    } catch (Exception e) {
      log.error("메시지 처리 실패: {}", e.getMessage());
    }
  }

  private ChatResponseDto convertToDto(Map<String, String> map) {
    return objectMapper.convertValue(map, ChatResponseDto.class);
  }
}

/**
 * Redis Stream을 소비(consume)하는 역할을 하는 클래스
 * Redis 스트림에 들어오는 데이터를 읽고 처리하며, aiService를 통해 AI 응답 처리를 담당
 *
 *  •  aiService: 실제로 Redis에서 받은 메시지를 처리하는 서비스
 *  •  streamContainer: Redis Stream을 수신(listen)하고 처리하는 역할을 하는 객체입니다. Spring Data Redis에서 제공하는 StreamMessageListenerContainer
 *
 *
 *  받는 것만(consume only)!!!!
 *  보내는 역할(produce)"는 RedisStreamService.sendToAI()
 */