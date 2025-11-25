package com.example.payflow.security.presentation.dto;

import com.example.payflow.security.domain.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    private String username;
    private String password;
    private String email;
    
    // 회원 유형
    private UserType userType;  // STORE_OWNER 또는 DISTRIBUTOR
    
    // 사업자 정보
    private String businessNumber;  // 사업자등록번호
    private String businessName;    // 상호명
    private String ownerName;       // 대표자명
    private String phoneNumber;     // 연락처
    private String address;         // 주소
}
