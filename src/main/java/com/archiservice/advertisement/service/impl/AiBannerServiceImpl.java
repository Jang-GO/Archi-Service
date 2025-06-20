package com.archiservice.advertisement.service.impl;

import com.archiservice.code.commoncode.service.CommonCodeService;
import com.archiservice.code.tagmeta.service.TagMetaService;
import com.archiservice.common.security.CustomUser;
import com.archiservice.exception.business.UserNotFoundException;
import com.archiservice.product.vas.domain.Vas;
import com.archiservice.product.vas.service.VasService;
import com.archiservice.code.tagmeta.component.TagConverter;
import com.archiservice.code.tagmeta.component.TagMappingComponent;
import com.archiservice.user.domain.User;
import com.archiservice.advertisement.dto.request.BannerRequestDto;
import com.archiservice.advertisement.dto.response.BannerResponseDto;
import com.archiservice.user.repository.UserRepository;
import com.archiservice.advertisement.service.AiBannerService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class AiBannerServiceImpl implements AiBannerService {

    private final UserRepository userRepository;
    private final TagMetaService tagMetaService;
    private final CommonCodeService commonCodeService;
    private final VasService vasService;
    private final TagMappingComponent tagMapping;
    private final TagConverter tagConverter;
    private final ChatClient bannerClient;

    public AiBannerServiceImpl(UserRepository userRepository, TagMetaService tagMetaService, CommonCodeService commonCodeService, VasService vasService, TagMappingComponent tagMapping, TagConverter tagConverter, @Qualifier("bannerClient") ChatClient bannerClient) {
        this.userRepository = userRepository;
        this.tagMetaService = tagMetaService;
        this.commonCodeService = commonCodeService;
        this.vasService = vasService;
        this.tagMapping = tagMapping;
        this.tagConverter = tagConverter;
        this.bannerClient = bannerClient;
    }

    @Override
    public BannerResponseDto getBanner(CustomUser customUser) {
        User user = userRepository.findById(customUser.getId())
                .orElseThrow(() -> new UserNotFoundException("올바른 사용자 정보를 가져오지 못했습니다."));

        List<String> myTagList = tagMetaService.extractTagsFromCode(user.getTagCode());

        String korSubTag = getRandSubtag(myTagList);
        String engSubTag = tagConverter.convertToEnglishKey(korSubTag);
        String engMainTag = tagMapping.getSubToMainMapping().get(engSubTag);
        String korMainTag = tagConverter.convertMainTagToKorean(engMainTag);

        String commonCode = commonCodeService.getCode("G03", korMainTag);

        Vas selectedVas = vasService.getRandVasByCategoryCode(commonCode);

        BannerRequestDto request = BannerRequestDto.builder()
                .vasId(selectedVas.getVasId())
                .mainTag(engMainTag)
                .subTag(korSubTag)
                .vasName(selectedVas.getVasName())
                .vasDescription(selectedVas.getVasDescription())
                .build();

        BannerResponseDto bannerResult = generateBanner(request);

        return bannerResult;
    }

    @Override
    public String getRandSubtag(List<String> myTags) {
        if (myTags == null || myTags.size() < 4) {
            return "";
        }

        int startIndex = 3;
        int endIndex = Math.min(myTags.size(), 8); // 3 + 5 = 8

        if (startIndex >= myTags.size()) {
            return "";
        }

        List<String> selectableTags = myTags.subList(startIndex, endIndex);

        List<String> subTagsOnly = selectableTags.stream()
                .filter(tag -> !tagMapping.getMainTags().contains(tag))
                .collect(Collectors.toList());

        if (subTagsOnly.isEmpty()) {
            return "";
        }

        Random random = new Random();
        int randomIndex = random.nextInt(selectableTags.size());

        return selectableTags.get(randomIndex);
    }

    @Override
    public BannerResponseDto generateBanner(BannerRequestDto request) {
        try {
            String fileName = "prompts/Banner_Prompt.txt";
            Path promptPath = new ClassPathResource(fileName).getFile().toPath();
            String promptTemplate = Files.readString(promptPath, StandardCharsets.UTF_8);

            return bannerClient.prompt()
                    .system(promptTemplate)
                    .user(u -> u.text("vasId = {vasId} 유지하고, 서브태그: {subTag}, 상품명: {vasName}에 맞는 배너를 생성해주세요")
                            .param("vasId", request.getVasId())
                            .param("subTag", request.getSubTag())
                            .param("vasName", request.getVasName())
                            .param("vasDescription", request.getVasDescription()))
                    .call()
                    .entity(BannerResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException("배너 생성 중 오류가 발생했습니다", e);
        }
    }
}
