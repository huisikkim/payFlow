package com.example.payflow.chatbot.application;

import com.example.payflow.chatbot.domain.Intent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
public class ResponseGenerator {

    private final Random random = new Random();

    private final Map<Intent, List<String>> responses = Map.of(
        Intent.GREETING, List.of(
            "안녕하세요! 채용 공고 검색 챗봇입니다. 💼\n원하시는 회사를 찾아드릴게요!\n\n'채용 찾기' 또는 '일자리 검색'이라고 말씀해주세요.",
            "반갑습니다! 지역, 업종, 연봉 조건으로 맞춤 채용 공고를 찾아드립니다. 😊\n\n'채용'이라고 입력하시면 시작할 수 있어요!"
        ),
        Intent.JOB_SEARCH_START, List.of(
            "채용 공고 검색을 시작하겠습니다. 🔍\n\n먼저, 어느 지역에서 일하고 싶으신가요?\n(예: 서울, 경기, 부산 등)"
        ),
        Intent.HELP, List.of(
            "💼 채용 공고 검색 챗봇 사용법\n\n" +
            "1️⃣ '채용' 또는 '일자리'라고 입력하여 검색 시작\n" +
            "2️⃣ 원하는 지역 선택 (예: 서울, 경기)\n" +
            "3️⃣ 관심 업종 선택 (예: IT, 금융)\n" +
            "4️⃣ 희망 연봉 입력 (예: 3000만원~5000만원)\n" +
            "5️⃣ 맞춤 채용 공고 확인!\n\n" +
            "'다시' 또는 '처음부터'라고 입력하면 검색을 다시 시작할 수 있어요."
        ),
        Intent.RESTART_SEARCH, List.of(
            "검색을 처음부터 다시 시작하겠습니다! 🔄\n\n어느 지역에서 일하고 싶으신가요?"
        ),
        Intent.UNKNOWN, List.of(
            "죄송합니다. 잘 이해하지 못했어요. 😅\n'도움말'을 입력하시면 사용법을 안내해드립니다.",
            "명확하게 이해하지 못했습니다.\n'채용'이라고 입력하시면 일자리 검색을 시작할 수 있어요!"
        )
    );

    public String generate(Intent intent) {
        List<String> intentResponses = responses.get(intent);
        if (intentResponses == null || intentResponses.isEmpty()) {
            return responses.get(Intent.UNKNOWN).get(0);
        }
        return intentResponses.get(random.nextInt(intentResponses.size()));
    }

    public String generateWithContext(Intent intent, String context) {
        String baseResponse = generate(intent);
        if (context != null && !context.isEmpty()) {
            return baseResponse + "\n\n" + context;
        }
        return baseResponse;
    }
}
