package com.example.payflow.youtube.application;

import com.example.payflow.youtube.domain.TrendingTopic;
import com.example.payflow.youtube.infrastructure.GoogleTrendsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleTrendsService {

    private final GoogleTrendsClient googleTrendsClient;

    /**
     * 한국 실시간 트렌드 조회
     */
    public List<TrendingTopic> getDailyTrends() {
        return googleTrendsClient.getDailyTrends();
    }
}
