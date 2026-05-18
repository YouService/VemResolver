package com.marketplace.servicos.security;

import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

public class OnlineTrackingFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public OnlineTrackingFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        chain.doFilter(request, response);
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User user) {
                LocalDateTime now = LocalDateTime.now();
                userRepository.atualizarUltimoAcesso(user.getId(), now, now.minusMinutes(2));
            }
        } catch (Exception ignored) {
            // Nunca bloquear a requisição por falha de rastreamento
        }
    }
}
