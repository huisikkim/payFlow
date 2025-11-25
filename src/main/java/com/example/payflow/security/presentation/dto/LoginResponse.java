package com.example.payflow.security.presentation.dto;

import com.example.payflow.security.domain.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String accessToken;
    private String tokenType;
    private String username;
    private Long userId;
    private UserType userType;      // 회원 유형
    private String businessName;    // 상호명
}
