package com.example.payflow.security.presentation;

import com.example.payflow.security.domain.User;
import com.example.payflow.security.domain.UserRepository;
import com.example.payflow.security.infrastructure.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            
            String token = jwtTokenProvider.generateToken(authentication);
            
            // 사용자 정보 조회하여 닉네임 포함
            User user = userRepository.findByUsername(request.getUsername())
                    .orElse(null);
            
            String nickname = (user != null && user.getNickname() != null) 
                    ? user.getNickname() 
                    : request.getUsername();
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "accessToken", token,
                    "username", request.getUsername(),
                    "nickname", nickname
            ));
        } catch (AuthenticationException e) {
            log.error("Login failed for user: {}", request.getUsername(), e);
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "아이디 또는 비밀번호가 올바르지 않습니다."
            ));
        }
    }
    
    // DTO
    public static class LoginRequest {
        private String username;
        private String password;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
    }
}
