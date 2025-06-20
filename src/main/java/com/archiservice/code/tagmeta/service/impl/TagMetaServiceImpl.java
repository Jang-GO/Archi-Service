package com.archiservice.code.tagmeta.service.impl;

import com.archiservice.code.tagmeta.domain.TagMeta;
import com.archiservice.code.tagmeta.repository.TagMetaRepository;
import com.archiservice.code.tagmeta.service.TagMetaService;
import io.jsonwebtoken.lang.Collections;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagMetaServiceImpl implements TagMetaService {
    private final TagMetaRepository tagMetaRepository;

    private Map<Integer, TagMeta> tagMetaCache = new ConcurrentHashMap<>();
    private final Map<String, TagMeta> tagMetaKeyCache = new ConcurrentHashMap<>();

    @EventListener(ApplicationReadyEvent.class)
    public void loadAllTagMetas() {
        List<TagMeta> allTagMetas = tagMetaRepository.findAll();
        tagMetaCache = allTagMetas.stream()
                .collect(Collectors.toConcurrentMap(
                        TagMeta::getBitPosition,
                        Function.identity()
                ));

        allTagMetas.forEach(meta ->
                tagMetaKeyCache.put(meta.getId().getTagKey().toLowerCase(), meta)
        );
    }

    @Override
    public List<String> extractTagsFromCode(Long tagCode) {
        if (tagCode == null || tagCode == 0) {
            return Collections.emptyList();
        }

        List<Integer> activeBitPositions = getActiveBitPositions(tagCode);

        if (activeBitPositions.isEmpty()) {
            return Collections.emptyList();
        }

        return activeBitPositions.stream()
                .map(tagMetaCache::get)
                .filter(Objects::nonNull)
                .map(TagMeta::getTagDescription)
                .collect(Collectors.toList());
    }

    @Override
    public Long calculateTagCodeFromKey(List<String> tagKeys) {
        if (tagKeys == null || tagKeys.isEmpty()) {
            throw new IllegalArgumentException("태그 리스트가 비어 있습니다.");
        }

        List<TagMeta> tagMetas = tagKeys.stream()
                .map(this::findTagMetaByKey)
                .filter(Objects::nonNull)
                .toList();

        if (tagMetas.isEmpty()) {
            throw new IllegalArgumentException("유효한 태그가 하나도 없습니다.");
        }

        return tagMetas.stream()
                .map(TagMeta::getBitPosition)
                .map(position -> 1L << position)
                .reduce(0L, (a, b) -> a | b);
    }




    private List<Integer> getActiveBitPositions(Long tagCode) {
        List<Integer> positions = new ArrayList<>();

        for (int position = 0; position < 64; position++) {
            if ((tagCode & (1L << position)) != 0) {
                positions.add(position);
            }
        }

        return positions;
    }
  
    @Override
    public TagMeta findTagMetaByKey(String key) {
        return tagMetaKeyCache.get(key.toLowerCase());
    }


}
