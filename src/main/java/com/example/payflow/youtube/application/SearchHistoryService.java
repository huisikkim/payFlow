package com.example.payflow.youtube.application;

import com.example.payflow.youtube.domain.SearchHistory;
import com.example.payflow.youtube.infrastructure.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;

    public void saveSearchHistory(String username, String searchQuery, int resultCount) {
        try {
            log.info("SearchHistoryService.saveSearchHistory 호출 - username: {}, query: {}", username, searchQuery);
            SearchHistory searchHistory = SearchHistory.builder()
                    .username(username)
                    .searchQuery(searchQuery)
                    .searchedAt(LocalDateTime.now())
                    .resultCount(resultCount)
                    .build();
            searchHistoryRepository.save(searchHistory);
            log.info("검색 기록 저장 완료");
        } catch (Exception e) {
            log.error("검색 기록 저장 중 에러 발생", e);
            throw e;
        }
    }

    public List<SearchHistory> getSearchHistory(String username) {
        return searchHistoryRepository.findByUsername(username);
    }

    public List<SearchHistory> getSearchHistory(String username, int limit) {
        return searchHistoryRepository.findByUsername(username, limit);
    }

    public void deleteSearchHistory(Long id, String username) {
        searchHistoryRepository.deleteById(id, username);
    }

    public void clearAllSearchHistory(String username) {
        searchHistoryRepository.deleteAllByUsername(username);
    }
}
