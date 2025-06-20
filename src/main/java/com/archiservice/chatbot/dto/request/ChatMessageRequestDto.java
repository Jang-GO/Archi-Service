package com.archiservice.chatbot.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequestDto {
  private String content;

  public static ChatMessageRequestDto of(String message){
    return builder()
        .content(message)
        .build();
  }
}