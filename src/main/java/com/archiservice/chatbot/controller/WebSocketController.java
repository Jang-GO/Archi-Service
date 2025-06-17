package com.archiservice.chatbot.controller;

import com.archiservice.chatbot.domain.AuthInfo;
import com.archiservice.chatbot.dto.request.ChatMessageRequestDto;
import com.archiservice.chatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final ChatService chatService;

    @MessageMapping("/chat/sendMessage")
    public void sendMessage(
            @Payload ChatMessageRequestDto message,
            AuthInfo authInfo
    ) {
            chatService.handleUserMessage(message, authInfo);
    }
}