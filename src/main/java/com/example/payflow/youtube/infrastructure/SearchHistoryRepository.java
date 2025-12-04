package com.example.payflow.youtube.infrastructure;

import com.example.payflow.youtube.domain.SearchHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SearchHistoryRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<SearchHistory> rowMapper = new RowMapper<SearchHistory>() {
        @Override
        public SearchHistory mapRow(ResultSet rs, int rowNum) throws SQLException {
            return SearchHistory.builder()
                    .id(rs.getLong("id"))
                    .username(rs.getString("username"))
                    .searchQuery(rs.getString("search_query"))
                    .searchedAt(rs.getTimestamp("searched_at").toLocalDateTime())
                    .resultCount(rs.getInt("result_count"))
                    .build();
        }
    };

    public void save(SearchHistory searchHistory) {
        String sql = "INSERT INTO youtube_search_history (username, search_query, searched_at, result_count) " +
                     "VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                searchHistory.getUsername(),
                searchHistory.getSearchQuery(),
                searchHistory.getSearchedAt(),
                searchHistory.getResultCount());
        log.info("검색 기록 저장 - username: {}, query: {}", 
                searchHistory.getUsername(), searchHistory.getSearchQuery());
    }

    public List<SearchHistory> findByUsername(String username, int limit) {
        String sql = "SELECT * FROM youtube_search_history " +
                     "WHERE username = ? " +
                     "ORDER BY searched_at DESC " +
                     "LIMIT ?";
        return jdbcTemplate.query(sql, rowMapper, username, limit);
    }

    public List<SearchHistory> findByUsername(String username) {
        return findByUsername(username, 50);
    }

    public void deleteById(Long id, String username) {
        String sql = "DELETE FROM youtube_search_history WHERE id = ? AND username = ?";
        jdbcTemplate.update(sql, id, username);
        log.info("검색 기록 삭제 - id: {}, username: {}", id, username);
    }

    public void deleteAllByUsername(String username) {
        String sql = "DELETE FROM youtube_search_history WHERE username = ?";
        jdbcTemplate.update(sql, username);
        log.info("모든 검색 기록 삭제 - username: {}", username);
    }
}
