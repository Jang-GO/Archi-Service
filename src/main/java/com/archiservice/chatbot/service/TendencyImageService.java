package com.archiservice.chatbot.service;

import com.archiservice.chatbot.dto.response.TendencyImageResultDto;
import com.archiservice.common.security.CustomUser;
import org.springframework.web.multipart.MultipartFile;

public interface TendencyImageService {

    void sendImageForAnalysis(CustomUser customUser, MultipartFile image);
    void handleTendencyImageResult(TendencyImageResultDto dto);

}
