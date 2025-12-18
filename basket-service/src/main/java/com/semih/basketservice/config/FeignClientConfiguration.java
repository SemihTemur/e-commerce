package com.semih.basketservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import com.semih.basketservice.exception.UnauthorizedException;

import java.util.stream.Collectors;

@Configuration
public class FeignClientConfiguration {

    @Bean
    public RequestInterceptor userHeaderInterceptor(){
        return (RequestTemplate requestTemplate)->{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if(authentication!=null && authentication.isAuthenticated()){
                String userId = authentication.getName();

                String rolesString = authentication.getAuthorities()
                        .stream().map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));

                if(StringUtils.hasText(userId) && StringUtils.hasText(rolesString)){
                    requestTemplate.header("User_id", userId);
                    requestTemplate.header("User_Roles", rolesString);
                }
            }else{
                throw new UnauthorizedException("Authentication required");
            }
        };
    }

}
