package com.archiservice.advertisement.service.impl;

import com.archiservice.advertisement.dto.request.BannerClickRequest;
import com.archiservice.chatbot.domain.AuthInfo;
import com.archiservice.chatbot.dto.request.ChatMessageRequestDto;
import com.archiservice.chatbot.service.ChatService;
import com.archiservice.common.security.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdvertisementService {

  private final ChatService chatService;

  public void handleBannerClick(CustomUser customUser, BannerClickRequest request) {

    Long userId = customUser.getId();
    String ageCode = customUser.getAgeCode();
    Long tagCode = customUser.getTagCode();

    String vasName = request.getVasName();
    String message = String.format("부가서비스 %s 알려줘", vasName);

    ChatMessageRequestDto dto = ChatMessageRequestDto.of(message);
    AuthInfo authInfo = AuthInfo.of(userId, ageCode, tagCode);

    chatService.handleUserMessage(dto, authInfo);
  }
}
