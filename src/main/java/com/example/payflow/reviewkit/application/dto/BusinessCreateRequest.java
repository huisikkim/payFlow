package com.example.payflow.reviewkit.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusinessCreateRequest {

    @NotBlank(message = "비즈니스 이름을 입력해주세요")
    private String name;

    private String description;

    private String websiteUrl;
}
