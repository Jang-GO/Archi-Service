package com.archiservice.common.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class AIConfig {

    @Bean
    ChatClient recommendChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                        You are a recommendation evaluation system for telecom products.
                        
                        You will be given the following information:
                        - User preference tags: <tagList>
                        - Recommended product list: <recommend>
                          - `plans`: A list of recommended mobile plans (in current ranked order)
                          - `vass`: A list of recommended value-added services (VAS)
                          - `coupons`: A list of recommended lifestyle coupons
                        
                        Your tasks are:
                        1. Evaluate the appropriateness of each recommendation by comparing the user's preferences with the `tags` of each recommended item.
                        2. If the current order is not appropriate within any of the three lists (`plans`, `vass`, or `coupons`), reorder the items accordingly.
                        3. Write a single, combined explanation in the `description` field that summarizes in Korean like kind agent:
                           - Why the recommendation order was appropriate (if unchanged), and
                           - Why it was rearranged (if changed), with reasoning based on user preferences and tag matches.
                        
                        ⚠️ Output strictly as a raw JSON object. Do **not** include:
                        - Code blocks (e.g. ```json),
                        - Markdown,
                        - Explanatory text,
                        - Any wrapping or formatting.
                        - Any escape characters such as backslashes (`\\`).
                        
                        The output must be a pure JSON object like this:
                        {"plans":[{"planId":39,"planName":"LTE청소년19","price":20900,"monthData":0,"callUsage":"133분","messageUsage":"1,000건","benefit":"U⁺ 모바일tv 라이트 무료","tags":["데이터 사용량이 적은 사용자","통화 사용량이 적은 사용자","문자(SMS) 사용량이 많은 사용자","LTE 사용자"],"category":"LTE","targetAge":"전체"},{"planId":25,"planName":"시니어16.5","price":16500,"monthData":0,"callUsage":"70분","messageUsage":"100건","benefit":"실버지킴이","tags":["데이터 사용량이 적은 사용자","통화 사용량이 적은 사용자","문자(SMS) 사용량이 많은 사용자","LTE 사용자"],"category":"LTE","targetAge":"연장자"},{"planId":15,"planName":"(LTE) 데이터 시니어 33","price":33000,"monthData":2,"callUsage":"무제한","messageUsage":"기본제공","benefit":"실버지킴이","tags":["데이터 사용량이 적은 사용자","통화 사용량이 많은 사용자","LTE 사용자"],"category":"LTE","targetAge":"연장자"},{"planId":8,"planName":"5G 라이트+","price":66000,"monthData":14,"callUsage":"무제한","messageUsage":"기본제공","benefit":"U⁺ 모바일tv 라이트 무료","tags":["데이터 사용량이 적은 사용자","통화 사용량이 많은 사용자","5G 사용자"],"category":"5G","targetAge":"전체"},{"planId":16,"planName":"유쓰 5G 슬림+","price":47000,"monthData":15,"callUsage":"무제한","messageUsage":"기본제공","benefit":"U⁺ 모바일tv라이트 무료","tags":["데이터 사용량이 적은 사용자","통화 사용량이 많은 사용자","5G 사용자"],"category":"5G","targetAge":"청년"}],"vass":[{"vasId":42,"vasName":"CGV 2D 영화관람권 2매 + CGV콤보","price":37000,"discountedPrice":28490,"saleRate":23,"imageUrl":null,"vasDescription":"최신 영화를 최대 혜택으로 즐기세요!","tags":["가족","데이트","영화","친구"],"category":"영화","onSale":true},{"vasId":44,"vasName":"CGV 2D 영화관람권 1매 + 스몰세트","price":20000,"discountedPrice":15600,"saleRate":22,"imageUrl":null,"vasDescription":"최신 영화를 최대 혜택으로 즐겨보세요!","tags":["가족","데이트","영화","친구"],"category":"영화","onSale":true},{"vasId":48,"vasName":"CGV 2D 영화관람권 1매","price":13000,"discountedPrice":9880,"saleRate":24,"imageUrl":null,"vasDescription":"전국 모든 CGV에서 2D 영화를 더욱 저렴한 가격으로 볼 수 있어요","tags":["가족","데이트","영화","친구"],"category":"영화","onSale":true},{"vasId":46,"vasName":"CGV 2D 영화관람권 3매","price":39000,"discountedPrice":29640,"saleRate":24,"imageUrl":null,"vasDescription":"전국 모든 CGV에서 2D 영화를 더욱 저렴한 가격으로 볼 수 있어요","tags":["가족","데이트","영화","친구"],"category":"영화","onSale":true},{"vasId":31,"vasName":"와우회원 혜택","price":7890,"discountedPrice":7496,"saleRate":5,"imageUrl":null,"vasDescription":"오늘 주문하면 내일 도착하는 빠른 배송과 무료 반품도 가능해요.","tags":["OTT","가족","배달","생활","자취","축구"],"category":"배달","onSale":true}],"coupons":[{"couponId":23,"couponName":"이모티콘플러스 월 구독권","price":0,"imageUrl":null,"tags":["데이터 사용량이 적은 사용자","통화 사용량이 적은 사용자","문자(SMS) 사용량이 많은 사용자","5G 사용자","LTE 사용자","해외 로밍 사용 경험이 많은 사용자","요금제를 자주 바꾸는 사용자","E북","IT","K-POP","OTT","가족","게임","경제","교육","넷플릭스","뉴스","데이트","도서","디즈니","마블","미드","반려동물","배달","사진","생활","쇼핑","식품","야구","여행","영어","영화","온라인 클래스","외식","웹소설","웹툰","유튜브","음악","자기개발","자취","주식","중국어","차량","축구","취미","친구","카페","커피","키즈","티빙","디저트","빵","편의점","오디오북","의류","SNS"],"category":"생활"},{"couponId":12,"couponName":"스노우 VIP이용권","price":0,"imageUrl":null,"tags":["통화 사용량이 많은 사용자","통화 사용량이 적은 사용자","문자(SMS) 사용량이 많은 사용자","5G 사용자","LTE 사용자","해외 로밍 사용 경험이 많은 사용자","요금제를 자주 바꾸는 사용자","E북","IT","K-POP","OTT","가족","게임","경제","교육","넷플릭스","뉴스","데이트","도서","디즈니","마블","미드","반려동물","배달","생활","친구","SNS"],"category":"생활"},{"couponId":10,"couponName":"스토리텔 오디오북 무제한구독권","price":0,"imageUrl":null,"tags":["데이터 사용량이 적은 사용자","게임","경제","넷플릭스","데이트","디즈니","반려동물","배달","식품","야구","영어","영화","온라인 클래스","외식","유튜브","음악","자기개발","주식","중국어","축구","취미","친구","카페","커피","오디오북"],"category":"도서"},{"couponId":17,"couponName":"파파존스 라지피자 30% 할인","price":0,"imageUrl":null,"tags":["가족","배달","외식"],"category":"배달"},{"couponId":20,"couponName":"cgv 팝콘+음료","price":0,"imageUrl":null,"tags":["가족","데이트","영화","친구"],"category":"생활"}], "description":"추천 순위를 조정한 이유를 간결하게 설명"}
                        
                        """)
                .build();
    }

    @Bean
    ChatClient reviewCleanBot(ChatClient.Builder builder) {
        return builder
                .defaultSystem(new ClassPathResource("prompts/moderation-rule-prompt.txt"))
                .build();
    }
}
