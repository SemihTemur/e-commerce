package com.semih.gateway.service;

import com.semih.gateway.exception.AuthenticationCredentialsNotFoundException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;

@Service
public class JwtTokenService {
    private final Key key;

    public JwtTokenService(@Value("${jwt.secret}") String jwtSecretKey) {
        this.key = Keys.hmacShaKeyFor(jwtSecretKey.getBytes());
    }

    public Long getUserIdByToken(String token){
        return Long.parseLong(Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject()
        );
    }

    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        }catch (ExpiredJwtException ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT token süresi dolmuş.");
        } catch (UnsupportedJwtException ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT token desteklenmiyor.");
        } catch (MalformedJwtException ex) {
            throw new AuthenticationCredentialsNotFoundException("Geçersiz JWT token.");
        } catch (SignatureException ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT token imzası doğrulanamadı.");
        } catch (IllegalArgumentException ex) {
            throw new AuthenticationCredentialsNotFoundException("Boş JWT token.");
        }
    }

}
