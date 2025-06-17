package com.archiservice.badword.service;

import com.archiservice.badword.domain.AhoCorasickAutomaton;
import com.archiservice.badword.repository.AllowedWordRepository;
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
    private final AllowedWordRepository allowedWordRepository;
    private final RedisTemplate<String, Set<String>> redisTemplate;

    private volatile AhoCorasickAutomaton automaton;
    private volatile Set<String> allowedWords;

    private static final String REDIS_BAD_WORDS_KEY = "bad_words";
    private static final String REDIS_ALLOWED_WORDS_KEY = "allowed_words";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    @PostConstruct
    public void initializeService() {
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

            loadAllowedWordsFromCache();

            automaton = new AhoCorasickAutomaton();
            int patternIndex = 0;

            for (String word : badWords) {
                automaton.addPattern(word.toLowerCase(), patternIndex++);
            }

            automaton.buildFailureLinks();
            log.info("Aho-Corasick 오토마톤 구축 완료: {} 개 패턴, {} 개 허용단어",
                    badWords.size(), allowedWords != null ? allowedWords.size() : 0);

        } catch (Exception e) {
            log.error("오토마톤 구축 실패", e);
        }
    }

    private void loadAllowedWordsFromCache() {
        Set<String> cachedAllowedWords = redisTemplate.opsForValue().get(REDIS_ALLOWED_WORDS_KEY);

        if (cachedAllowedWords != null) {
            this.allowedWords = cachedAllowedWords;
            log.debug("Redis에서 허용단어 {} 개 로드", allowedWords.size());
            return;
        }

        List<String> dbAllowedWords = allowedWordRepository.findAllWords();
        this.allowedWords = new HashSet<>(dbAllowedWords);

        redisTemplate.opsForValue().set(REDIS_ALLOWED_WORDS_KEY, allowedWords, CACHE_TTL);
        log.info("DB에서 허용단어 {} 개 로드 및 Redis 캐싱 완료", allowedWords.size());
    }

    public boolean containsBadWord(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        if (automaton == null || allowedWords == null) {
            log.warn("오토마톤이 초기화되지 않음. 동기식으로 초기화 진행");
            buildAutomaton();
        }

        String normalizedContent = normalizeContent(content);
        List<AhoCorasickAutomaton.MatchResult> matches = automaton.search(normalizedContent);

        return matches.stream()
                .anyMatch(match -> !isAllowedViolation(content, match));
    }

    public List<String> findBadWords(String content) {
        if (content == null) return Collections.emptyList();

        if (automaton == null || allowedWords == null) {
            buildAutomaton();
        }

        String normalizedContent = normalizeContent(content);
        List<AhoCorasickAutomaton.MatchResult> matches = automaton.search(normalizedContent);

        return matches.stream()
                .filter(match -> !isAllowedViolation(content, match))
                .map(AhoCorasickAutomaton.MatchResult::getPattern)
                .distinct()
                .collect(Collectors.toList());
    }

    private boolean isAllowedViolation(String originalContent, AhoCorasickAutomaton.MatchResult match) {
        int start = match.getStartIndex();
        int end = match.getEndIndex() + 1;

        String fullWord = extractWordBoundary(originalContent, start, end);
        return allowedWords.contains(fullWord.toLowerCase());
    }

    private String extractWordBoundary(String text, int start, int end) {
        while (start > 0 && isWordCharacter(text.charAt(start - 1))) {
            start--;
        }

        while (end < text.length() && isWordCharacter(text.charAt(end))) {
            if (isKoreanParticle(text, end)) {
                break;
            }
            end++;
        }

        return text.substring(start, end);
    }

    private boolean isKoreanParticle(String text, int position) {
        String[] particles = {"이", "가", "을", "를", "은", "는", "에", "에서", "로", "으로"};
        for (String particle : particles) {
            if (text.substring(position).startsWith(particle)) {
                return true;
            }
        }
        return false;
    }


    private boolean isWordCharacter(char c) {
        return Character.isLetterOrDigit(c) ||
                (c >= 0xAC00 && c <= 0xD7AF);
    }

    private Set<String> loadBadWordsFromCache() {
        Set<String> cachedWords = redisTemplate.opsForValue().get(REDIS_BAD_WORDS_KEY);
        if (cachedWords != null && !cachedWords.isEmpty()) {
            log.debug("Redis에서 불용어 {} 개 로드", cachedWords.size());
            return cachedWords;
        }

        List<String> dbWords = badWordRepository.findAllWords();
        Set<String> wordSet = new HashSet<>(dbWords);

        redisTemplate.opsForValue().set(REDIS_BAD_WORDS_KEY, wordSet, CACHE_TTL);
        log.info("DB에서 불용어 {} 개 로드 및 Redis 캐싱 완료", wordSet.size());

        return wordSet;
    }

    private String normalizeContent(String content) {
        return content.toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("[^가-힣a-zA-Z0-9]", "");
    }

    @Scheduled(fixedRate = 43200000)
    public void refreshAutomaton() {
        buildAutomaton();
    }
}

