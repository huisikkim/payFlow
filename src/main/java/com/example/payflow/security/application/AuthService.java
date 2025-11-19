package com.example.payflow.security.application;

import com.example.payflow.security.domain.Role;
import com.example.payflow.security.domain.User;
import com.example.payflow.security.domain.UserRepository;
import com.example.payflow.security.infrastructure.jwt.JwtTokenProvider;
import com.example.payflow.security.presentation.dto.LoginRequest;
import com.example.payflow.security.presentation.dto.LoginResponse;
import com.example.payflow.security.presentation.dto.SignupRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .roles(Set.of(Role.ROLE_USER))
                .enabled(true)
                .build();
        
        userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String token = jwtTokenProvider.generateToken(authentication);
        
        // 사용자 정보 조회
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .username(request.getUsername())
                .userId(user.getId())
                .build();
    }
}
