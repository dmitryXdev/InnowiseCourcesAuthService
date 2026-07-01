package com.innowise.authservice.security.filter;

import com.innowise.authservice.jwt.JwtProvider;
import com.innowise.authservice.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(token == null || !token.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        token = token.substring(7);

        if(!jwtProvider.validateToken(token)) {
           response.setStatus(HttpStatus.FORBIDDEN.value());
           return;
        }

       Jwt jwt = jwtProvider.getJwtInstanceFromString(token);

       UserPrincipal principal = UserPrincipal.builder()
               .id(Long.parseLong(jwt.getSubject()))
               .role(jwt.getClaim("role"))
               .build();

       Authentication authentication = new UsernamePasswordAuthenticationToken(
               principal,
               null,
               List.of(new SimpleGrantedAuthority("ROLE_" + principal.getRole())));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
