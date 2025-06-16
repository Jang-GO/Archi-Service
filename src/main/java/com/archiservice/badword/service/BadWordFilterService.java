package com.archiservice.badword.service;

import com.archiservice.badword.domain.AhoCorasickAutomaton;
import com.archiservice.badword.repository.BadWordRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class BadWordFilterService {

    private final BadWordRepository badWordRepository;
    private final RedisTemplate<String, Set<String>> redisTemplate;

    private volatile AhoCorasickAutomaton automaton;
    private static final String REDIS_KEY = "bad_words";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    @PostConstruct
    public void initializeService() {
        log.info("금칙어 필터링 서비스 초기화 시작");
        CompletableFuture.runAsync(this::buildAutomaton)
                .thenRun(() -> log.info("Aho-Corasick 오토마톤 초기화 완료"))
                .exceptionally(throwable -> {
                    log.error("오토마톤 초기화 실패", throwable);
                    return null;
                });
    }

    private void buildAutomaton() {
        try {
            Set<String> badWords = loadBadWordsFromCache();

            // Aho-Corasick 오토마톤 구축
            automaton = new AhoCorasickAutomaton();
            int patternIndex = 0;

            for (String word : badWords) {
                automaton.addPattern(word.toLowerCase(), patternIndex++);
            }

            automaton.buildFailureLinks();
            log.info("Aho-Corasick 오토마톤 구축 완료: {} 개 패턴", badWords.size());

        } catch (Exception e) {
            log.error("오토마톤 구축 실패", e);
        }
    }

    private Set<String> loadBadWordsFromCache() {
        // 1. Redis에서 조회
        Set<String> cachedWords = redisTemplate.opsForValue().get(REDIS_KEY);
        if (cachedWords != null && !cachedWords.isEmpty()) {
            log.debug("Redis에서 불용어 {} 개 로드", cachedWords.size());
            return cachedWords;
        }

        List<String> dbWords = badWordRepository.findAllWords();
        Set<String> wordSet = new HashSet<>(dbWords);

        redisTemplate.opsForValue().set(REDIS_KEY, wordSet, CACHE_TTL);
        log.info("DB에서 불용어 {} 개 로드 및 Redis 캐싱 완료", wordSet.size());

        return wordSet;
    }

    public boolean containsBadWord(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        if (automaton == null) {
            log.warn("오토마톤이 초기화되지 않음. 동기식으로 초기화 진행");
            buildAutomaton();
        }

        String normalizedContent = normalizeContent(content);

        // Aho-Corasick로 빠른 검색
        List<AhoCorasickAutomaton.MatchResult> matches = automaton.search(normalizedContent);
        return !matches.isEmpty();
    }

    public List<String> findBadWords(String content) {
        if (content == null) return Collections.emptyList();

        if (automaton == null) {
            buildAutomaton();
        }

        String normalizedContent = normalizeContent(content);
        List<AhoCorasickAutomaton.MatchResult> matches = automaton.search(normalizedContent);

        return matches.stream()
                .map(AhoCorasickAutomaton.MatchResult::getPattern)
                .distinct()
                .collect(Collectors.toList());
    }

    private String normalizeContent(String content) {
        return content.toLowerCase()
                .replaceAll("\\s+", "")  // 공백 제거
                .replaceAll("[^가-힣a-zA-Z0-9]", ""); // 특수문자 제거
    }

    // 12시간마다 금칙어 갱신 (TTL 만료 전에 미리 갱신)
    @Scheduled(fixedRate = 43200000)
    public void refreshAutomaton() {
        buildAutomaton();
    }
}
