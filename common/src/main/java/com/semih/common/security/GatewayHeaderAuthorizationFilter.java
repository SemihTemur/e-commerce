package com.semih.common.security;

import com.semih.common.exception.MissingAuthorizationHeaderException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class GatewayHeaderAuthorizationFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("User_id");
        String rolesHeader = request.getHeader("User_Roles");

        if(StringUtils.hasText(userId) && StringUtils.hasText(rolesHeader)){

            Set<SimpleGrantedAuthority> authorities = Arrays.stream(rolesHeader.split(","))
                    .map(role -> new SimpleGrantedAuthority(role.trim().toUpperCase()))
                    .collect(Collectors.toSet());

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    authorities
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request,response);
    }
}
