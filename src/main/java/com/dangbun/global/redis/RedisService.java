package com.dangbun.global.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redisTemplate;

    public void setValues(String key, String data, Duration duration) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, data, duration);
    }

    @Transactional(readOnly = true)
    public String getValues(String key){
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        if(values.get(key) == null){
            return "false";
        }
        return values.get(key);
    }

    public void deleteValues(String key){
        redisTemplate.delete(key);
    }

    public void expireValues(String key, int timeout){
        redisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);
    }

    public void setHashOps(String key, Map<String,String> data){
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        values.putAll(key, data);
    }

    @Transactional(readOnly = true)
    public String getHashOps(String key, String hashKey){
        HashOperations<String,Object, Object> values = redisTemplate.opsForHash();
        return values.hasKey(key, hashKey) ?(String) values.get(key,hashKey): "";
    }

    public void deleteHashOps(String key, String hashKey){
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        values.delete(key, hashKey);
    }

    public boolean checkExistsValue(String value){
        return !value.equals("false");
    }

    public void addRecentSearch(String key, String value, int maxSize) {
        redisTemplate.opsForList().remove(key, 0, value);
        redisTemplate.opsForList().leftPush(key, value);
        redisTemplate.opsForList().trim(key, 0, maxSize - 1);
    }

    public List<String> getRecentSearches(String key, int maxSize) {
        return redisTemplate.opsForList().range(key, 0, maxSize - 1);
    }

    public String getRedisKey(Long placeId, Long memberId) {
        return "recent:search:" + placeId + ":" + memberId;
    }

}
