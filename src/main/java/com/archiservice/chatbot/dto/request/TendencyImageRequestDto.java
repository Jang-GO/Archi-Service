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
  private String userId;
  private String base64Image;

  public static TendencyImageRequestDto of(String userId, String base64Image) {
    return new TendencyImageRequestDto(userId, base64Image);
  }
}
