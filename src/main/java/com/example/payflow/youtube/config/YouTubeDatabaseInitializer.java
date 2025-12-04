package com.example.payflow.youtube.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class YouTubeDatabaseInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            // YouTube 검색 기록 테이블 생성
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS youtube_search_history (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    username VARCHAR(100) NOT NULL,
                    search_query VARCHAR(500) NOT NULL,
                    searched_at TIMESTAMP NOT NULL,
                    result_count INT DEFAULT 0
                )
                """;
            
            jdbcTemplate.execute(createTableSql);
            log.info("✅ YouTube 검색 기록 테이블 생성 완료");
            
            // 인덱스 생성
            try {
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_username ON youtube_search_history(username)");
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_searched_at ON youtube_search_history(searched_at)");
                log.info("✅ YouTube 검색 기록 인덱스 생성 완료");
            } catch (Exception e) {
                log.debug("인덱스는 이미 존재합니다.");
            }
            
        } catch (Exception e) {
            log.error("YouTube 데이터베이스 초기화 실패", e);
        }
    }
}
