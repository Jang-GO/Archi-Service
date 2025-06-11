package com.archiservice.chatbot.service.impl;

import com.archiservice.chatbot.domain.AuthInfo;
import com.archiservice.chatbot.domain.Chat;
import com.archiservice.chatbot.dto.AiMessage;
import com.archiservice.chatbot.dto.AuthMetadata;
import com.archiservice.chatbot.dto.ChatMessageDto;
import com.archiservice.chatbot.dto.ChatResponseDto;
import com.archiservice.chatbot.dto.type.Sender;
import com.archiservice.chatbot.redis.RedisStreamService;
import com.archiservice.chatbot.repository.ChatRepository;
import com.archiservice.chatbot.service.AiService;
import com.archiservice.user.domain.User;
import com.archiservice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

  private final ChatRepository chatRepository;
  private final UserRepository userRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final RedisStreamService redisStreamService;

  @Override
  public void sendMessageToAI(Chat chat, AuthInfo authInfo) {
    ChatMessageDto requestDto = ChatMessageDto.fromChat(chat);

    AuthMetadata metadata = new AuthMetadata(authInfo.getTagCode(), authInfo.getAgeCode());
    AiMessage aiMessage = new AiMessage(metadata, requestDto);

    redisStreamService.sendToAI(aiMessage);
  }

  @Override
  public void handleAIResponse(ChatResponseDto aiResponse) {
    // DB 저장
    User user = userRepository.findById(aiResponse.getUserId()).orElseThrow();
    Chat chat = Chat.builder()
        .user(user)
        .sender(Sender.BOT)
        .message(aiResponse.getContent())
        .messageType(aiResponse.getType())
        .build();
    chatRepository.save(chat);

    // 타입별 추가 처리
    switch(aiResponse.getType()) {
      case SUGGESTION:
        // bundleRecommendations 처리
        aiResponse.setContent("[추천] " + aiResponse.getContent());
        break;
      case KEYWORD_RECOMMENDATION:
        // extractedKeywords, bundleRecommendations 처리
        aiResponse.setContent("[키워드] " + aiResponse.getContent());
        break;
      case PREFERENCE_UPDATE:
        // userService.updatePreference(aiResponse.getUserId(), aiResponse.getUpdatedPreference());
        aiResponse.setContent("[성향 업데이트] " + aiResponse.getContent());
        break;
    }

    messagingTemplate.convertAndSendToUser(
        String.valueOf(aiResponse.getUserId()),
        "/queue/chat",
        aiResponse
    );
  }
}