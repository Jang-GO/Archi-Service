package com.archiservice.code.commoncode.service.impl;

import com.archiservice.code.commoncode.domain.CommonCode;
import com.archiservice.code.commoncode.repository.CommonCodeRepository;
import com.archiservice.code.commoncode.service.CommonCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCodeServiceImpl implements CommonCodeService {
    private final CommonCodeRepository commonCodeRepository;

    private Map<String, ConcurrentMap<String, String>> commonCodeCache = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void loadAllCommonCodes() {
        List<CommonCode> allCommonCodes = commonCodeRepository.findAll();
        commonCodeCache = allCommonCodes.stream()
                .collect(Collectors.groupingBy(
                        CommonCode::getGroupCode,
                        Collectors.toConcurrentMap(
                                CommonCode::getCommonCode,
                                CommonCode::getCommonName
                        )
                ));
    }

    @Override
    public String getCodeName(String groupCode, String commonCode) {
        ConcurrentMap<String, String> groupMap = commonCodeCache.get(groupCode);

        if (groupMap == null) {
            return commonCode;
        }
        return groupMap.getOrDefault(commonCode, commonCode);
    }

    @Override
    public String getCode(String groupCode, String commonName) {

        ConcurrentMap<String, String> groupMap = commonCodeCache.get(groupCode);
        if (groupMap != null) {
            String result = groupMap.get(commonName);
            if (result != null) {
                return result;
            }
        }

        return commonCodeRepository.findCommonCodeByGroupCodeAndCommonName(groupCode, commonName)
                .orElse(null);
    }

}
