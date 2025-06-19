package com.archiservice.chatbot.controller;

import com.archiservice.chatbot.service.TendencyImageService;
import com.archiservice.common.response.ApiResponse;
import com.archiservice.common.security.CustomUser;
import com.archiservice.chatbot.service.impl.TendencyImageServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/tendency")
@RequiredArgsConstructor
public class TendencyImageController {
    private final TendencyImageService tendencyImageService;

    @PostMapping("/image")
    public ResponseEntity<ApiResponse<Void>> analyzeImage(
        @AuthenticationPrincipal CustomUser customUser,
        @RequestParam MultipartFile image) {

        tendencyImageService.sendImageForAnalysis(customUser, image);
        return ResponseEntity.ok(ApiResponse.success("이미지 분석 요청 완료", null));
    }
}
