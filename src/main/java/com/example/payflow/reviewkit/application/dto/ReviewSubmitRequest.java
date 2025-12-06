package com.example.payflow.reviewkit.application.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewSubmitRequest {

    @NotBlank(message = "이름을 입력해주세요")
    private String reviewerName;

    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String reviewerEmail;

    private String reviewerCompany; // Optional

    @NotNull(message = "별점을 선택해주세요")
    @Min(value = 1, message = "별점은 1~5 사이여야 합니다")
    @Max(value = 5, message = "별점은 1~5 사이여야 합니다")
    private Integer rating;

    @Size(max = 2000, message = "리뷰 내용은 2000자를 초과할 수 없습니다")
    private String content;
}
