package com.archiservice.chatbot.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.archiservice.chatbot.dto.ChatMessageDto;
import com.archiservice.chatbot.service.ChatService;
import com.archiservice.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chats")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/history/{userId}")
    public ResponseEntity<ApiResponse<List<ChatMessageDto>>> getChatHistory(
            @PathVariable("userId") Long userId,
            @RequestParam("page") int page,
            @RequestParam("size") int size ) {
        List<ChatMessageDto> data = chatService.loadChatHistory(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success("성공했습니다.", data));
    }
    

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<ApiResponse<String>> clearChatHistory(@PathVariable("userId") Long userId) {
        chatService.deleteChatByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("성공했습니다.", "채팅 기록 초기화 완료"));
    }


}
