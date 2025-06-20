package com.archiservice.code.tagmeta.component;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class TagConverter {

    private static final Map<String, String> SUB_TAG_KOREAN_TO_ENGLISH; // 서브태그
    private static final Map<String, String> MAIN_TAG_ENGLISH_TO_KOREAN; // 메인태그 변환

    static {
        // 서브태그 한글→영문 매핑
        Map<String, String> korToEng = new HashMap<>();
        // IT
        korToEng.put("IT", "it");
        // OTT
        korToEng.put("OTT", "ott");
        korToEng.put("넷플릭스", "netflix");
        korToEng.put("디즈니", "disney");
        korToEng.put("티빙", "tving");
        korToEng.put("미드", "drama");
        // News
        korToEng.put("뉴스", "news");
        korToEng.put("경제", "economy");
        korToEng.put("주식", "stock");
        // Book
        korToEng.put("E북", "ebook");
        // Pet
        korToEng.put("반려동물", "pet");
        // Delivery
        korToEng.put("배달", "delivery");
        // Photo
        korToEng.put("사진", "photo");
        // Life
        korToEng.put("자취", "living");
        korToEng.put("쇼핑", "shopping");
        korToEng.put("편의점", "store");
        korToEng.put("가족", "family");
        korToEng.put("친구", "friend");
        korToEng.put("데이트", "date");
        // Food
        korToEng.put("외식", "dining");
        korToEng.put("식품", "food");
        // Car
        korToEng.put("차량", "car");
        // Movie
        korToEng.put("영화", "movie");
        // OnlineClass
        korToEng.put("온라인 클래스", "onlineClass");
        korToEng.put("자기개발", "growth");
        korToEng.put("영어", "english");
        korToEng.put("중국어", "chinese");
        korToEng.put("교육", "education");
        // Webtoon
        korToEng.put("웹툰", "webtoon");
        korToEng.put("웹소설", "webnovel");
        // Youtube
        korToEng.put("유튜브", "youtube");
        // Hobby
        korToEng.put("야구", "baseball");
        korToEng.put("축구", "soccer");
        korToEng.put("게임", "game");
        // Cafe
        korToEng.put("카페", "cafe");
        korToEng.put("커피", "coffee");
        korToEng.put("빵", "bread");
        // Music
        korToEng.put("음악", "music");
        korToEng.put("K-POP", "kpop");
        // Kids
        korToEng.put("키즈", "kids");

        SUB_TAG_KOREAN_TO_ENGLISH = Collections.unmodifiableMap(korToEng);

        // 메인태그(서비스 분류) 영문→한글 매핑
        Map<String, String> mainTagMap = new HashMap<>();
        mainTagMap.put("IT", "IT");
        mainTagMap.put("OTT", "OTT");
        mainTagMap.put("News", "뉴스");
        mainTagMap.put("Book", "도서");
        mainTagMap.put("Pet", "반려동물");
        mainTagMap.put("Delivery", "배달");
        mainTagMap.put("Photo", "사진");
        mainTagMap.put("Life", "생활");
        mainTagMap.put("Food", "식품");
        mainTagMap.put("Car", "차량");
        mainTagMap.put("Movie", "영화");
        mainTagMap.put("OnlineClass", "온라인 클래스");
        mainTagMap.put("Webtoon", "웹툰");
        mainTagMap.put("Youtube", "유튜브");
        mainTagMap.put("Music", "음악");
        mainTagMap.put("Hobby", "취미");
        mainTagMap.put("Cafe", "카페");
        mainTagMap.put("Kids", "키즈");

        MAIN_TAG_ENGLISH_TO_KOREAN = Collections.unmodifiableMap(mainTagMap);
    }

    public String convertToEnglishKey(String koreanTag) {
        String englishKey = SUB_TAG_KOREAN_TO_ENGLISH.get(koreanTag);
        if (englishKey == null) {
            return koreanTag;
        }
        return englishKey;
    }

    public boolean hasMapping(String koreanTag) {
        return SUB_TAG_KOREAN_TO_ENGLISH.containsKey(koreanTag);
    }

    public String convertMainTagToKorean(String englishMainTag) {
        String koreanMainTag = MAIN_TAG_ENGLISH_TO_KOREAN.get(englishMainTag);
        if (koreanMainTag == null) {
            return englishMainTag;
        }
        return koreanMainTag;
    }

    public boolean hasMainTagMapping(String englishMainTag) {
        return MAIN_TAG_ENGLISH_TO_KOREAN.containsKey(englishMainTag);
    }
}