package com.example.payflow.specification.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SynonymRequest {
    private String standardName;
    private String synonym;
    private Double similarityScore;
}
