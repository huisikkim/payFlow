package com.example.payflow.security.config;

import com.example.payflow.security.infrastructure.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/saga/**").permitAll()  // Saga 테스트 API 허용
                        .requestMatchers("/api/test/**").permitAll()  // 카프카 테스트 API 허용
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()  // Static 리소스 허용
                        .requestMatchers("/", "/index", "/payment", "/success", "/fail").permitAll()
                        .requestMatchers("/stages", "/stages/**").permitAll()
                        .requestMatchers("/chatbot", "/api/chatbot/**").permitAll()
                        .requestMatchers("/logs/dashboard", "/api/logs/**").permitAll()  // 로그 대시보드 허용
                        .requestMatchers("/api/session-replay/events").permitAll()  // 이벤트 수집 API 허용
                        .requestMatchers("/session-replay/**", "/api/session-replay/**").permitAll()  // 세션 재생 (개발용: 모두 허용)
                        .requestMatchers("/sourcing", "/api/sourcing/**").permitAll()  // 토탈소싱기 허용
                        .requestMatchers("/escrow/**").permitAll()  // 에스크로 웹 페이지 허용 (API보다 먼저)
                        .requestMatchers("/api/escrow/**").permitAll()  // 에스크로 API 허용 (개발용)
                        // TODO: 프로덕션에서는 .requestMatchers("/api/escrow/**").hasAnyRole("USER", "ADMIN")으로 변경
                        .requestMatchers("/api/orders/**").permitAll()  // 주문 API 허용 (개발용)
                        .requestMatchers("/api/payments/**").permitAll()  // 결제 API 허용 (개발용)
                        // TODO: 프로덕션에서는 .requestMatchers("/api/orders/**", "/api/payments/**").hasAnyRole("USER", "ADMIN")으로 변경
                        .requestMatchers("/hr/**").permitAll()  // HR 웹 페이지 허용 (개발용)
                        .requestMatchers("/api/hr/**").hasAnyRole("USER", "ADMIN")  // HR API (인증 필요)
                        .requestMatchers("/recruitment/**").permitAll()  // 채용 시스템 웹 페이지 허용 (개발용)
                        .requestMatchers("/api/recruitment/**").hasAnyRole("USER", "ADMIN")  // 채용 시스템 API (인증 필요)
                        .requestMatchers("/ainjob/**").permitAll()  // AINJOB 웹 페이지 허용 (개발용)
                        .requestMatchers("/api/ainjob/**").permitAll()  // AINJOB API 허용 (개발용)
                        // TODO: 프로덕션에서는 .requestMatchers("/api/ainjob/**").hasAnyRole("USER", "ADMIN")으로 변경
                        .requestMatchers("/api/products/**").permitAll()  // Pick Swap 상품 API 허용 (개발용)
                        // TODO: 프로덕션에서는 .requestMatchers("/api/products/**").hasAnyRole("USER", "ADMIN")으로 변경
                        .requestMatchers("/api/stages/**").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        // H2 Console을 위한 설정
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        
        return http.build();
    }
}
