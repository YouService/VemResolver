package com.marketplace.servicos.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_TENTATIVAS = 10;
    private static final long JANELA_MS = 60_000L;

    private record Contador(AtomicInteger tentativas, long inicioJanela) {}

    private final ConcurrentHashMap<String, Contador> contadores = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (!"/api/auth/login".equals(request.getRequestURI()) || !"POST".equals(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String ip = resolverIp(request);
        long agora = System.currentTimeMillis();

        Contador contador = contadores.compute(ip, (k, c) -> {
            if (c == null || agora - c.inicioJanela() > JANELA_MS) {
                return new Contador(new AtomicInteger(1), agora);
            }
            c.tentativas().incrementAndGet();
            return c;
        });

        if (contador.tentativas().get() > MAX_TENTATIVAS) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"message\":\"Muitas tentativas de login. Tente novamente em 1 minuto.\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String resolverIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
