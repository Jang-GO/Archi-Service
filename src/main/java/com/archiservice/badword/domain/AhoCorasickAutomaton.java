package com.archiservice.badword.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

public class AhoCorasickAutomaton {

    private TrieNode root;
    private List<String> patterns;

    public AhoCorasickAutomaton() {
        this.root = new TrieNode();
        this.patterns = new ArrayList<>();
    }

    public void addPattern(String pattern, int index) {
        patterns.add(pattern);
        TrieNode current = root;

        for (char c : pattern.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new TrieNode());
        }

        current.output.add(index);
    }

    public void buildFailureLinks() {
        Queue<TrieNode> queue = new ArrayDeque<>();

        // 루트의 자식들 초기화
        for (TrieNode child : root.children.values()) {
            child.failure = root;
            queue.offer(child);
        }

        // BFS로 failure link 구축
        while (!queue.isEmpty()) {
            TrieNode current = queue.poll();

            for (Map.Entry<Character, TrieNode> entry : current.children.entrySet()) {
                char c = entry.getKey();
                TrieNode child = entry.getValue();

                TrieNode failure = current.failure;
                while (failure != null && !failure.children.containsKey(c)) {
                    failure = failure.failure;
                }

                if (failure == null) {
                    child.failure = root;
                } else {
                    child.failure = failure.children.get(c);
                    child.output.addAll(child.failure.output);
                }

                queue.offer(child);
            }
        }
    }

    public List<MatchResult> search(String text) {
        List<MatchResult> results = new ArrayList<>();
        TrieNode current = root;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // failure link를 따라 이동
            while (current != null && !current.children.containsKey(c)) {
                current = current.failure;
            }

            if (current == null) {
                current = root;
                continue;
            }

            current = current.children.get(c);

            // 매칭된 패턴들 수집
            for (Integer patternIndex : current.output) {
                String pattern = patterns.get(patternIndex);
                results.add(new MatchResult(i - pattern.length() + 1, i, pattern));
            }
        }

        return results;
    }

    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        TrieNode failure;
        List<Integer> output = new ArrayList<>();
    }

    @Data
    @AllArgsConstructor
    public static class MatchResult {
        private int startIndex;
        private int endIndex;
        private String pattern;
    }
}

