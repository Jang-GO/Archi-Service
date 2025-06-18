package com.archiservice.chatbot.dto;

import com.archiservice.chatbot.domain.Chat;
import com.archiservice.chatbot.dto.type.MessageType;
import com.archiservice.chatbot.dto.type.Sender;
import java.time.LocalDateTime;
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
public class ChatMessageDto {
  private String messageId;
  private Long userId;
  private String content;
  private MessageType type;
  private Sender sender;
  private LocalDateTime timestamp;

  public static ChatMessageDto fromChat(Chat chat) {
    return ChatMessageDto.builder()
        .messageId(chat.getChatId().toString())
        .userId(chat.getUser().getUserId())
        .content(chat.getMessage())
        .type(chat.getMessageType())
        .sender(chat.getSender())
        .timestamp(chat.getCreatedAt())
        .build();
  }

  public static ChatMessageDto infoMessage(Long userId, String content){
    return ChatMessageDto.builder()
        .messageId(null)
        .userId(userId)
        .content(content)
        .type(MessageType.INFO)
        .sender(Sender.BOT)
        .timestamp(LocalDateTime.now())
        .build();
  }
}
