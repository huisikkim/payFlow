package com.example.payflow.stage.presentation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JoinStageRequest {

    @NotNull(message = "순번은 필수입니다.")
    @Min(value = 1, message = "순번은 1 이상이어야 합니다.")
    private Integer turnNumber;
}
