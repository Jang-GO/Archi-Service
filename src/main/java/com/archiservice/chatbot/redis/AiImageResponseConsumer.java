package com.archiservice.chatbot.redis;

import com.archiservice.chatbot.dto.response.TendencyImageResultDto;
import com.archiservice.chatbot.service.impl.TendencyImageServiceImpl;
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

/**
 * Redis Stream에서 메시지를 수신하여 처리.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiImageResponseConsumer {

  private final TendencyImageServiceImpl tendencyImageService;
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

  private void handleMessage(MapRecord<String, String, String> record) {
    try {
      String streamId = record.getId().getValue();
      String key = "processed:image-response:" + streamId;

      Boolean first = redisTemplate.opsForValue().setIfAbsent(key, "1", java.time.Duration.ofMinutes(10));
      if (Boolean.FALSE.equals(first)) {
        log.info("[Consumer] 이미 처리된 메시지: {}", streamId);
        redisTemplate.opsForStream().acknowledge("image-response-handler", record);
        return;
      }

      TendencyImageResultDto resultDto = convertToDto(record.getValue());
      tendencyImageService.handleTendencyImageResult(resultDto);

      redisTemplate.opsForStream().acknowledge("image-response-handler", record);

    } catch (Exception e) {
      log.error("이미지 성향 메시지 처리 실패: {}", e.getMessage(), e);
    }
  }


  private TendencyImageResultDto convertToDto(Map<String, String> map) {
    return objectMapper.convertValue(map, TendencyImageResultDto.class);
  }


}
