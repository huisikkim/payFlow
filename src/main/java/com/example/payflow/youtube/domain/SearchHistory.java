package com.example.payflow.youtube.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {
    private Long id;
    private String username;
    private String searchQuery;
    private LocalDateTime searchedAt;
    private int resultCount;
}
