package com.example.payflow.security.infrastructure.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean shouldSkip = path.startsWith("/parlevel") || 
               path.startsWith("/api/parlevel") ||
               path.startsWith("/api/auth") ||
               // path.startsWith("/api/catalog") ||  // 제거: 인증이 필요한 API
               // path.startsWith("/api/cart") ||  // 제거: 인증이 필요한 API
               // path.startsWith("/api/catalog-orders") ||  // 제거: 인증이 필요한 API
               path.startsWith("/login") ||
               path.startsWith("/signup") ||
               path.startsWith("/h2-console") ||
               path.startsWith("/css") ||
               path.startsWith("/js") ||
               path.startsWith("/images");
        
        log.debug("shouldNotFilter - path: {}, skip: {}", path, shouldSkip);
        return shouldSkip;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            String path = request.getRequestURI();
            
            log.debug("=== JWT Filter - Processing Request ===");
            log.debug("Path: {}", path);
            log.debug("JWT present: {}", StringUtils.hasText(jwt));
            
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.getUsernameFromToken(jwt);
                log.debug("JWT valid - username: {}", username);
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                log.debug("UserDetails loaded - authorities: {}", userDetails.getAuthorities());
                
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                                userDetails, 
                                null, 
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authentication set in SecurityContext");
            } else {
                log.debug("JWT validation failed or not present");
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
