package com.archiservice.chatbot.redis;

import com.archiservice.chatbot.dto.request.TendencyImageRequestDto;
import com.archiservice.exception.business.AiMessageSendFailedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Service;

/**
 * 외부로부터 받은 입력을 Redis Stream에 발행만
 */
@Service
@RequiredArgsConstructor
public class AiImageRequestProducer {
  private final RedisTemplate<String, Object> redisTemplate;
  private static final String STREAM_KEY = "image-request-stream";
  private final ObjectMapper objectMapper;
  private final StreamOperations<String, Object, Object> streamOperations;

  public void sendToAI(TendencyImageRequestDto dto) {
    try{
      String json = objectMapper.writeValueAsString(dto);
      Map<String, Object> message = Map.of("data", json);

      streamOperations.add(STREAM_KEY, message);

    }catch (JsonProcessingException e) {
      throw new AiMessageSendFailedException("AI 이미지 요청 직렬화 실패");
    }
  }
}
