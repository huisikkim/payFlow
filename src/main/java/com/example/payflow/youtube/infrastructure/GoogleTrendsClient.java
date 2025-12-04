package com.example.payflow.youtube.infrastructure;

import com.example.payflow.youtube.domain.TrendingTopic;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class GoogleTrendsClient {

    // Google Trends RSS 피드 URL (권장 방식)
    private static final String RSS_URL_TEMPLATE = "https://trends.google.com/trending/rss?geo=%s";
    private static final int TIMEOUT_MS = 10000;
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    /**
     * Google Trends RSS 피드에서 실시간 트렌드 조회
     * 캐시 10분
     */
    @Cacheable(value = "googleTrends", key = "'KR'")
    public List<TrendingTopic> getDailyTrends() {
        return getDailyTrends("KR");
    }

    /**
     * 특정 국가의 실시간 트렌드 가져오기
     * @param countryCode ISO 3166-2 국가 코드 (예: KR, US, JP)
     * @return 트렌드 아이템 리스트
     */
    public List<TrendingTopic> getDailyTrends(String countryCode) {
        String rssUrl = String.format(RSS_URL_TEMPLATE, countryCode);
        List<TrendingTopic> trends = new ArrayList<>();

        try {
            log.info("Google Trends RSS 피드 조회 시작: {}", rssUrl);

            // RSS 피드 파싱
            Document doc = Jsoup.connect(rssUrl)
                    .timeout(TIMEOUT_MS)
                    .userAgent(USER_AGENT)
                    .get();

            Elements items = doc.select("item");
            log.info("Google Trends RSS에서 {}개 항목 발견", items.size());

            int rank = 1;
            for (Element item : items) {
                TrendingTopic topic = parseTrendItem(item, rank++);
                if (topic != null) {
                    trends.add(topic);
                }
            }

            log.info("Google Trends 조회 완료 - {}개 항목", trends.size());

        } catch (IOException e) {
            log.error("Google Trends RSS 피드 조회 실패: {}", e.getMessage());
            // 실패 시 빈 리스트 반환 (또는 캐시된 데이터 사용)
        }

        return trends;
    }

    /**
     * RSS item 요소를 TrendingTopic으로 파싱
     */
    private TrendingTopic parseTrendItem(Element item, int rank) {
        try {
            String title = getTextContent(item, "title");
            if (title == null || title.isEmpty()) {
                return null;
            }

            // 트래픽 정보 (ht:approx_traffic)
            String traffic = getTextContent(item, "ht|approx_traffic");
            if (traffic != null && !traffic.isEmpty()) {
                traffic = traffic + " 검색";
            }

            // 발행 날짜 파싱
            LocalDateTime publishedAt = parsePublishDate(getTextContent(item, "pubDate"));

            // 이미지 URL (ht:picture)
            String imageUrl = getTextContent(item, "ht|picture");

            // 뉴스 정보 (첫 번째 뉴스 아이템)
            String newsUrl = null;
            String description = null;
            Element newsItem = item.selectFirst("ht|news_item");
            if (newsItem != null) {
                newsUrl = getTextContent(newsItem, "ht|news_item_url");
                description = getTextContent(newsItem, "ht|news_item_title");
            }

            // 뉴스 URL이 없으면 Google 검색 링크로 대체
            if (newsUrl == null || newsUrl.isEmpty()) {
                newsUrl = "https://www.google.com/search?q=" + title;
            }

            return TrendingTopic.builder()
                    .title(title)
                    .description(description != null ? description : "")
                    .newsUrl(newsUrl)
                    .imageUrl(imageUrl)
                    .traffic(traffic)
                    .publishedAt(publishedAt)
                    .rank(rank)
                    .build();

        } catch (Exception e) {
            log.warn("트렌드 아이템 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Element에서 텍스트 추출 (null-safe)
     */
    private String getTextContent(Element parent, String selector) {
        Elements elements = parent.select(selector);
        if (elements.isEmpty()) {
            return null;
        }
        String text = elements.first().text();
        return text != null && !text.isEmpty() ? text : null;
    }

    /**
     * RSS pubDate 파싱 (한국 시간대로 변환)
     * 형식: "Thu, 4 Dec 2025 04:00:00 -0800"
     */
    private LocalDateTime parsePublishDate(String pubDate) {
        if (pubDate == null || pubDate.isEmpty()) {
            return LocalDateTime.now();
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            ZonedDateTime zdt = ZonedDateTime.parse(pubDate, formatter);
            // 한국 시간대(KST)로 변환
            ZonedDateTime kstTime = zdt.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
            return kstTime.toLocalDateTime();
        } catch (Exception e) {
            log.debug("날짜 파싱 실패, 현재 시간 사용: {}", pubDate);
            return LocalDateTime.now();
        }
    }
}
