package com.example.payflow.sourcing.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class NaverDataLabService {
    
    @Value("${naver.datalab.client-id:}")
    private String clientId;
    
    @Value("${naver.datalab.client-secret:}")
    private String clientSecret;
    
    private final Gson gson = new Gson();
    
    /**
     * 네이버 데이터랩 API로 검색어 트렌드 조회
     * @param keyword 검색 키워드
     * @return 트렌드 데이터 (평균 검색 비율 0-100)
     */
    public Map<String, Object> getSearchTrend(String keyword) {
        Map<String, Object> result = new HashMap<>();
        
        // API 키가 설정되지 않은 경우 시뮬레이션 데이터 반환
        if (clientId == null || clientId.isEmpty() || clientSecret == null || clientSecret.isEmpty()) {
            log.warn("네이버 데이터랩 API 키가 설정되지 않았습니다. 시뮬레이션 데이터를 반환합니다.");
            return getSimulatedData(keyword);
        }
        
        try {
            String apiUrl = "https://openapi.naver.com/v1/datalab/search";
            
            // 최근 1개월 데이터 조회
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusMonths(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            
            // 요청 바디 생성
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("startDate", startDate.format(formatter));
            requestBody.addProperty("endDate", endDate.format(formatter));
            requestBody.addProperty("timeUnit", "date");
            
            JsonArray keywordGroups = new JsonArray();
            JsonObject keywordGroup = new JsonObject();
            keywordGroup.addProperty("groupName", keyword);
            JsonArray keywords = new JsonArray();
            keywords.add(keyword);
            keywordGroup.add("keywords", keywords);
            keywordGroups.add(keywordGroup);
            
            requestBody.add("keywordGroups", keywordGroups);
            
            // HTTP 요청
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-Naver-Client-Id", clientId);
            conn.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            // 요청 바디 전송
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            // 응답 읽기
            int responseCode = conn.getResponseCode();
            BufferedReader br;
            
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
            }
            
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
            
            if (responseCode == 200) {
                // 응답 파싱
                JsonObject jsonResponse = gson.fromJson(response.toString(), JsonObject.class);
                JsonArray results = jsonResponse.getAsJsonArray("results");
                
                if (results != null && results.size() > 0) {
                    JsonObject firstResult = results.get(0).getAsJsonObject();
                    JsonArray data = firstResult.getAsJsonArray("data");
                    
                    // 평균 검색 비율 계산
                    double avgRatio = 0;
                    for (int i = 0; i < data.size(); i++) {
                        JsonObject dataPoint = data.get(i).getAsJsonObject();
                        avgRatio += dataPoint.get("ratio").getAsDouble();
                    }
                    avgRatio = avgRatio / data.size();
                    
                    // 상대적 검색 비율을 절대적 검색량으로 추정
                    // 주의: 네이버 데이터랩은 상대적 비율(0-100)만 제공하므로 추정치입니다
                    // 정확한 검색량은 네이버 검색광고 API 필요
                    long estimatedSearchVolume = estimateSearchVolumeFromRatio(avgRatio, keyword);
                    
                    result.put("searchVolume", estimatedSearchVolume);
                    result.put("trendRatio", avgRatio);
                    result.put("isEstimated", true);
                    result.put("dataSource", "NAVER_DATALAB");
                    
                    log.info("네이버 데이터랩 조회 성공 - 키워드: {}, 평균 비율: {}", keyword, avgRatio);
                } else {
                    log.warn("네이버 데이터랩 응답에 데이터가 없습니다.");
                    return getSimulatedData(keyword);
                }
            } else {
                log.error("네이버 데이터랩 API 오류 - 응답 코드: {}, 메시지: {}", responseCode, response);
                return getSimulatedData(keyword);
            }
            
        } catch (Exception e) {
            log.error("네이버 데이터랩 API 호출 실패: {}", e.getMessage());
            return getSimulatedData(keyword);
        }
        
        return result;
    }
    
    /**
     * 데이터랩 비율을 실제 검색량으로 추정
     * 주의: 이는 추정치이며 실제 검색량과 다를 수 있습니다
     * 
     * 추정 로직:
     * - 비율 0-20: 저검색량 (1,000 ~ 5,000)
     * - 비율 20-40: 중저검색량 (5,000 ~ 15,000)
     * - 비율 40-60: 중검색량 (15,000 ~ 30,000)
     * - 비율 60-80: 중고검색량 (30,000 ~ 60,000)
     * - 비율 80-100: 고검색량 (60,000 ~ 100,000+)
     */
    private long estimateSearchVolumeFromRatio(double ratio, String keyword) {
        // 키워드 특성에 따른 기본 배율 조정
        double multiplier = 300; // 기본 배율
        
        // 키워드 길이에 따른 조정 (짧을수록 검색량 많음)
        if (keyword.length() <= 5) {
            multiplier = 400;
        } else if (keyword.length() <= 10) {
            multiplier = 350;
        }
        
        // 비율에 따른 비선형 변환
        double adjustedRatio = ratio;
        if (ratio > 50) {
            // 높은 비율은 더 가파르게 증가
            adjustedRatio = ratio * 1.5;
        } else if (ratio < 20) {
            // 낮은 비율은 완만하게
            adjustedRatio = ratio * 0.8;
        }
        
        long estimatedVolume = (long) (adjustedRatio * multiplier);
        
        // 최소값 보장
        if (estimatedVolume < 1000) {
            estimatedVolume = 1000;
        }
        
        return estimatedVolume;
    }
    
    /**
     * 시뮬레이션 데이터 생성
     */
    private Map<String, Object> getSimulatedData(String keyword) {
        Map<String, Object> result = new HashMap<>();
        
        // 키워드 길이에 따른 검색량 추정
        int baseVolume = 5000;
        if (keyword.length() <= 5) {
            baseVolume = 20000;
        } else if (keyword.length() <= 10) {
            baseVolume = 10000;
        }
        
        // 키워드 해시값을 시드로 사용하여 일관된 랜덤값 생성
        int seed = keyword.hashCode();
        java.util.Random random = new java.util.Random(seed);
        long searchVolume = baseVolume + random.nextInt(baseVolume);
        
        result.put("searchVolume", searchVolume);
        result.put("trendRatio", searchVolume / 1000.0);
        result.put("dataSource", "SIMULATED");
        
        return result;
    }
}
