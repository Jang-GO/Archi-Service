package com.archiservice.chatbot.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TendencyImageRequestDto {
  private Long userId;
  private String base64Image;

  public static TendencyImageRequestDto of(Long userId, String base64Image) {
    return new TendencyImageRequestDto(userId, base64Image);
  }
}
