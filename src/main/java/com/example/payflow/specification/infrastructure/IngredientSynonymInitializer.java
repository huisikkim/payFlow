package com.example.payflow.specification.infrastructure;

import com.example.payflow.specification.domain.IngredientSynonym;
import com.example.payflow.specification.domain.IngredientSynonymRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 재료 동의어 초기 데이터 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IngredientSynonymInitializer implements CommandLineRunner {
    
    private final IngredientSynonymRepository repository;
    
    @Override
    @Transactional
    public void run(String... args) {
        if (repository.count() > 0) {
            log.info("재료 동의어 데이터가 이미 존재합니다. 초기화를 건너뜁니다.");
            return;
        }
        
        log.info("재료 동의어 초기 데이터 생성 시작...");
        
        List<IngredientSynonym> synonyms = new ArrayList<>();
        
        // 양파
        synonyms.add(createSynonym("양파", "양파", 1.0));
        synonyms.add(createSynonym("양파", "양 파", 1.0));
        synonyms.add(createSynonym("양파", "황양파", 0.95));
        synonyms.add(createSynonym("양파", "적양파", 0.9));
        synonyms.add(createSynonym("양파", "onion", 0.85));
        
        // 감자
        synonyms.add(createSynonym("감자", "감자", 1.0));
        synonyms.add(createSynonym("감자", "감 자", 1.0));
        synonyms.add(createSynonym("감자", "potato", 0.85));
        synonyms.add(createSynonym("감자", "포테이토", 0.85));
        
        // 당근
        synonyms.add(createSynonym("당근", "당근", 1.0));
        synonyms.add(createSynonym("당근", "당 근", 1.0));
        synonyms.add(createSynonym("당근", "carrot", 0.85));
        synonyms.add(createSynonym("당근", "캐럿", 0.85));
        
        // 대파
        synonyms.add(createSynonym("대파", "대파", 1.0));
        synonyms.add(createSynonym("대파", "대 파", 1.0));
        synonyms.add(createSynonym("대파", "파", 0.9));
        synonyms.add(createSynonym("대파", "쪽파", 0.8));
        synonyms.add(createSynonym("대파", "실파", 0.8));
        
        // 마늘
        synonyms.add(createSynonym("마늘", "마늘", 1.0));
        synonyms.add(createSynonym("마늘", "마 늘", 1.0));
        synonyms.add(createSynonym("마늘", "garlic", 0.85));
        synonyms.add(createSynonym("마늘", "깐마늘", 0.95));
        synonyms.add(createSynonym("마늘", "다진마늘", 0.9));
        
        // 닭가슴살
        synonyms.add(createSynonym("닭가슴살", "닭가슴살", 1.0));
        synonyms.add(createSynonym("닭가슴살", "닭 가슴살", 1.0));
        synonyms.add(createSynonym("닭가슴살", "치킨가슴살", 0.95));
        synonyms.add(createSynonym("닭가슴살", "chicken breast", 0.85));
        
        // 돼지고기
        synonyms.add(createSynonym("돼지고기", "돼지고기", 1.0));
        synonyms.add(createSynonym("돼지고기", "돼지 고기", 1.0));
        synonyms.add(createSynonym("돼지고기", "pork", 0.85));
        synonyms.add(createSynonym("돼지고기", "삼겹살", 0.8));
        synonyms.add(createSynonym("돼지고기", "목살", 0.8));
        
        // 소고기
        synonyms.add(createSynonym("소고기", "소고기", 1.0));
        synonyms.add(createSynonym("소고기", "소 고기", 1.0));
        synonyms.add(createSynonym("소고기", "beef", 0.85));
        synonyms.add(createSynonym("소고기", "우육", 0.9));
        
        // 배추
        synonyms.add(createSynonym("배추", "배추", 1.0));
        synonyms.add(createSynonym("배추", "배 추", 1.0));
        synonyms.add(createSynonym("배추", "김장배추", 0.95));
        synonyms.add(createSynonym("배추", "알배추", 0.9));
        
        // 무
        synonyms.add(createSynonym("무", "무", 1.0));
        synonyms.add(createSynonym("무", "radish", 0.85));
        synonyms.add(createSynonym("무", "무우", 0.95));
        
        // 고추
        synonyms.add(createSynonym("고추", "고추", 1.0));
        synonyms.add(createSynonym("고추", "고 추", 1.0));
        synonyms.add(createSynonym("고추", "청양고추", 0.9));
        synonyms.add(createSynonym("고추", "홍고추", 0.9));
        synonyms.add(createSynonym("고추", "풋고추", 0.9));
        
        // 양배추
        synonyms.add(createSynonym("양배추", "양배추", 1.0));
        synonyms.add(createSynonym("양배추", "양 배추", 1.0));
        synonyms.add(createSynonym("양배추", "cabbage", 0.85));
        synonyms.add(createSynonym("양배추", "캐비지", 0.85));
        
        // 토마토
        synonyms.add(createSynonym("토마토", "토마토", 1.0));
        synonyms.add(createSynonym("토마토", "tomato", 0.95));
        synonyms.add(createSynonym("토마토", "방울토마토", 0.9));
        
        // 오이
        synonyms.add(createSynonym("오이", "오이", 1.0));
        synonyms.add(createSynonym("오이", "cucumber", 0.85));
        synonyms.add(createSynonym("오이", "오 이", 1.0));
        
        // 버섯
        synonyms.add(createSynonym("버섯", "버섯", 1.0));
        synonyms.add(createSynonym("버섯", "버 섯", 1.0));
        synonyms.add(createSynonym("버섯", "mushroom", 0.85));
        synonyms.add(createSynonym("버섯", "느타리버섯", 0.9));
        synonyms.add(createSynonym("버섯", "팽이버섯", 0.9));
        synonyms.add(createSynonym("버섯", "새송이버섯", 0.9));
        
        // 계란
        synonyms.add(createSynonym("계란", "계란", 1.0));
        synonyms.add(createSynonym("계란", "계 란", 1.0));
        synonyms.add(createSynonym("계란", "달걀", 0.95));
        synonyms.add(createSynonym("계란", "egg", 0.85));
        
        // 우유
        synonyms.add(createSynonym("우유", "우유", 1.0));
        synonyms.add(createSynonym("우유", "우 유", 1.0));
        synonyms.add(createSynonym("우유", "milk", 0.85));
        
        // 쌀
        synonyms.add(createSynonym("쌀", "쌀", 1.0));
        synonyms.add(createSynonym("쌀", "rice", 0.85));
        synonyms.add(createSynonym("쌀", "백미", 0.95));
        synonyms.add(createSynonym("쌀", "현미", 0.9));
        
        // 밀가루
        synonyms.add(createSynonym("밀가루", "밀가루", 1.0));
        synonyms.add(createSynonym("밀가루", "밀 가루", 1.0));
        synonyms.add(createSynonym("밀가루", "flour", 0.85));
        synonyms.add(createSynonym("밀가루", "박력분", 0.9));
        synonyms.add(createSynonym("밀가루", "중력분", 0.9));
        synonyms.add(createSynonym("밀가루", "강력분", 0.9));
        
        // 설탕
        synonyms.add(createSynonym("설탕", "설탕", 1.0));
        synonyms.add(createSynonym("설탕", "설 탕", 1.0));
        synonyms.add(createSynonym("설탕", "sugar", 0.85));
        synonyms.add(createSynonym("설탕", "백설탕", 0.95));
        
        // 소금
        synonyms.add(createSynonym("소금", "소금", 1.0));
        synonyms.add(createSynonym("소금", "소 금", 1.0));
        synonyms.add(createSynonym("소금", "salt", 0.85));
        synonyms.add(createSynonym("소금", "천일염", 0.9));
        
        repository.saveAll(synonyms);
        
        log.info("재료 동의어 초기 데이터 생성 완료: {} 건", synonyms.size());
    }
    
    private IngredientSynonym createSynonym(String standardName, String synonym, Double score) {
        return IngredientSynonym.builder()
            .standardName(standardName)
            .synonym(synonym)
            .similarityScore(score)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
}
