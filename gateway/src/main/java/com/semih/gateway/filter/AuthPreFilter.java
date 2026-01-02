package com.semih.gateway.filter;

import com.semih.gateway.exception.AuthenticationCredentialsNotFoundException;
import com.semih.gateway.service.JwtTokenService;
import io.jsonwebtoken.ExpiredJwtException;
// Diğer gerekli JWT istisna importları eklenebilir:
// import io.jsonwebtoken.SignatureException;
// import io.jsonwebtoken.MalformedJwtException;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest; // Doğru import
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Component
@Order(-1)
public class AuthPreFilter implements GlobalFilter {

    private final JwtTokenService jwtTokenService;
    private final RedisTemplate<String,Object> redisTemplate;
    private final List<String> PUBLIC_URIS = List.of(
            "/login","/register","/refresh-token"
    );

    public AuthPreFilter(JwtTokenService jwtTokenService, RedisTemplate<String, Object> redisTemplate) {
        this.jwtTokenService = jwtTokenService;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (isPublicUri(path)) {
            return chain.filter(exchange);
        }

        HttpHeaders headers = request.getHeaders();
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new AuthenticationCredentialsNotFoundException(
                    "Authorization token missing or invalid format."));
        }

        String token = authHeader.substring(7);

        try {
            if (StringUtils.hasText(token) && jwtTokenService.validateToken(token)) {

                Object userIdObject = jwtTokenService.getUserIdByToken(token);
                String userId = String.valueOf(userIdObject);

                Set<String> roles = (Set<String>) redisTemplate.opsForHash()
                        .get("permission", userId);

                if (roles == null) {
                    return Mono.error(new AuthenticationCredentialsNotFoundException(
                            "Invalid token or access denied: User roles not found."));
                }

                String rolesString = String.join(",",roles);

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("User_id", userId)
                        .header("User_Roles",rolesString)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } else {
                // Token valid değilse ama hata fırlatılmadıysa
                return Mono.error(new AuthenticationCredentialsNotFoundException(
                        "Invalid token."));
            }
        } catch (ExpiredJwtException ex) {
            // JWT Kütüphanesi Hatalarını Yakala ve Sarmala
            return Mono.error(new AuthenticationCredentialsNotFoundException("JWT token süresi dolmuş."));
        } catch (Exception e) {
            // Diğer JWT/Redis/Servis hatalarını yakala
            return Mono.error(new AuthenticationCredentialsNotFoundException(
                    "Token validation failed: " + e.getMessage()));
        }
    }

    private boolean isPublicUri(String path) {
        return PUBLIC_URIS.stream().anyMatch(path::contains);
    }


}