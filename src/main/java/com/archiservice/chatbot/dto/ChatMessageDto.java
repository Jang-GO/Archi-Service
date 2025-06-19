package com.archiservice.chatbot.dto;

import java.time.LocalDateTime;

import com.archiservice.chatbot.domain.Chat;
import com.archiservice.chatbot.dto.type.MessageType;
import com.archiservice.chatbot.dto.type.Sender;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatMessageDto {
  private String messageId;
  @JsonAlias({"user_id", "userId"})
  private Long userId;
  @JsonAlias({"ai_response"})
  private String content;
  @JsonAlias({"message_type", "type"})
  private MessageType type;
  @JsonAlias({"mentioned_plans"})
  private String mentionedPlans;
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

  public static ChatMessageDto ofSummary(Long userId, String summary) {
    return ChatMessageDto.builder()
            .userId(userId)
            .content(summary)
            .type(MessageType.IMAGE_ANALYSIS)
            .sender(Sender.BOT)
            .timestamp(LocalDateTime.now())
            .build();
  }

  public static ChatMessageDto ofTags(Long userId, String tags) {
    return ChatMessageDto.builder()
            .userId(userId)
            .content(tags)
            .type(MessageType.IMAGE_ANALYSIS)
            .sender(Sender.BOT)
            .timestamp(LocalDateTime.now())
            .build();
  }



}

//content