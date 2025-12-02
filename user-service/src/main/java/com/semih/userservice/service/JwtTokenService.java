package com.semih.userservice.service;

import com.semih.userservice.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.SignatureException;


import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtTokenService {

    private final Key key;

    public JwtTokenService(@Value("${jwt.secret}") String jwtSecret) {
        this.key= Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(User user){
        Map<String,Object> claims = new HashMap<>();

        claims.put("id",user.getId());

        long JWT_EXPIRATION = 15 * 60 * 1000;
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getId().toString())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ JWT_EXPIRATION))
                .signWith(key)
                .compact();
    }

    public Long getUserIdByToken(String token){
        return Long.parseLong(Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject());

    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT token süresi dolmuş.", ex.fillInStackTrace());
        } catch (UnsupportedJwtException ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT token desteklenmiyor.", ex.fillInStackTrace());
        } catch (MalformedJwtException ex) {
            throw new AuthenticationCredentialsNotFoundException("Geçersiz JWT token.", ex.fillInStackTrace());
        } catch (SignatureException ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT token imzası doğrulanamadı.", ex.fillInStackTrace());
        } catch (IllegalArgumentException ex) {
            throw new AuthenticationCredentialsNotFoundException("Boş JWT token.", ex.fillInStackTrace());
        }
    }

}
