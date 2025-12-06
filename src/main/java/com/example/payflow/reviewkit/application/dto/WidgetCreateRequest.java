package com.example.payflow.reviewkit.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WidgetCreateRequest {

    @NotBlank(message = "위젯 이름을 입력해주세요")
    private String name;

    private String theme = "light"; // "light" or "dark"

    private String layout = "grid"; // "grid" or "list"

    private Integer displayLimit = 6;

    private String language = "ko"; // "ko" or "en"
}
