package com.semih.userservice.security;

import com.semih.userservice.entity.Role;
import com.semih.userservice.service.JwtTokenService;
import com.semih.userservice.util.RedisUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenService jwtTokenService;
    private final RedisUtil redisUtil;

    public JWTAuthenticationFilter(JwtTokenService jwtTokenService, RedisUtil redisUtil) {
        this.jwtTokenService = jwtTokenService;
        this.redisUtil = redisUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getJWTFromRequest(request);
        if(StringUtils.hasText(token) && jwtTokenService.validateToken(token)){
            String id = jwtTokenService.getUserIdByToken(token).toString();

            Set<String> permissions = redisUtil.getUserPermissionsFromCache("permission",id);

            if(permissions==null){
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Kullanıcıya ait yetki bulunamadı");
                return;
            }

            Set<Role> roles = permissions
                    .stream()
                    .map(Role::valueOf)
                    .collect(Collectors.toSet());

            Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
                id,null,roles
            );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        filterChain.doFilter(request,response);
    }

    private String getJWTFromRequest(HttpServletRequest httpServletRequest){
        String bearerToken = httpServletRequest.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken)){
            if(bearerToken.startsWith("Bearer ")){
                return bearerToken.substring(7);
            }
        }
        return null;
    }
}
