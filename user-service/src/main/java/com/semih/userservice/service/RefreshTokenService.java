package com.semih.userservice.service;

import com.semih.userservice.entity.RefreshToken;
import com.semih.userservice.entity.User;
import com.semih.userservice.exception.InvalidTokenException;
import com.semih.userservice.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();
    private static final int TOKEN_BYTE_LENGTH = 30;

    public String createRefreshTokenForUser(User user){
        RefreshToken token = new RefreshToken();

        token.setUser(user);
        token.setToken(generateRefreshTokenString());
        token.setExpiryDate(Instant.now().plusSeconds((long) 7 *24*3600));

        refreshTokenRepository.save(token);

        return token.getToken();
    }

    @Transactional(readOnly = true)
    public Optional<User> rotateRefreshToken(String refreshToken){
        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(()-> new InvalidTokenException("Geçersiz token!"));

        validateRefreshToken(refreshTokenEntity);

        deleteRefreshToken(refreshTokenEntity);

        return Optional.ofNullable(refreshTokenEntity.getUser());
    }

    public void deleteRefreshToken(RefreshToken refreshToken){
        refreshTokenRepository.delete(refreshToken);
    }

    private String generateRefreshTokenString(){
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];

        secureRandom.nextBytes(randomBytes);

        return base64Encoder.encodeToString(randomBytes);
    }

    private void validateRefreshToken(RefreshToken refreshTokenEntity){
        if(refreshTokenEntity.getExpiryDate().isBefore(Instant.now()))
            throw new InvalidTokenException("Geçersiz token!");
    }

}
