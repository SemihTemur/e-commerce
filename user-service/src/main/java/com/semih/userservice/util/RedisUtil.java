package com.semih.userservice.util;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@Component
public final class RedisUtil {

    private final RedisTemplate<String,Object> redisTemplate;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveUserPermissionsToCache(String key, String userId, Set<String> roles){
        redisTemplate.opsForHash().put(key,userId,roles);
    }

    public Set<String> getUserPermissionsFromCache(String key, String userId){
        return (Set<String>) redisTemplate.opsForHash().get("permission", id);
    }

    public void deleteUserPermissionsToCache(String key, String userId){
        redisTemplate.opsForHash().delete(key,userId);
    }
}
