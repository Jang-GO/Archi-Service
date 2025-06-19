package com.archiservice.chatbot.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TendencyImageRequestDto {
  private Map<String, Object> payload;
  private Map<String, Object> metadata;

  public static TendencyImageRequestDto of(String userId, String base64Image) {
    return new TendencyImageRequestDto(
            Map.of(
                    "userId", userId,
                    "image", base64Image
            ),
            Map.of(
                    "timestamp", Instant.now().toString()
            )
    );
  }
}


