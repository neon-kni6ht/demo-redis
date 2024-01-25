package com.example.demoredis.service;

import com.example.demoredis.exception.DataNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class RedisUserService {
    private final HashOperations<String, String, String> hashOperations;
    private final ZSetOperations<String, String> zSetOperations;
    @Value("${max-size}")
    private long maxSizePerUser;

    private final static long MAX_SIZE_PER_RECORD = 256;
    @Autowired
    public RedisUserService(RedisTemplate<String, String> redisTemplate) {
        this.hashOperations = redisTemplate.opsForHash();
        this.zSetOperations = redisTemplate.opsForZSet();
    }

    public void setUserData(String userID, String ID, String data) {
        validateData(userID, ID, data);

        checkStoredSize(userID,ID,data);

        zSetOperations.add("userA:" + userID,"id:" + ID,System.currentTimeMillis());
        hashOperations.put("userD:" + userID,"id:" + ID, data);
    }

    public String getUserData(String userID, String ID) {
        String data = hashOperations.get("userD:" + userID,"id:" + ID);

        if (data == null)
            throw new DataNotFoundException("No data found for ID " + ID);

        zSetOperations.add("userA:" + userID,"id:" + ID, System.currentTimeMillis());
        return data;
    }

    private void checkStoredSize(String userID, String ID, String data){
        Map<String,String> keys = hashOperations.entries("userD:" + userID);
        if (!keys.isEmpty()) {
            Map<String,String> cleanKeys = new HashMap<>(keys);
            cleanKeys.remove("id:"+ID);
            long storedSize = cleanKeys.values().stream()
                    .map(String::getBytes)
                    .mapToLong(it -> it.length)
                    .sum();
            if (maxSizePerUser>0 && storedSize + data.getBytes().length > maxSizePerUser && storedSize>0)
               evict(userID,ID,data);
            }
        }

    private synchronized void evict(String userID, String ID, String data) {
        Map<String, String> keys = hashOperations.entries("userD:" + userID);
        if (!keys.isEmpty()) {
            Map<String, String> cleanKeys = new HashMap<>(keys);
            cleanKeys.remove("id:" + ID);
            long storedSize = cleanKeys.values().stream()
                    .map(String::getBytes)
                    .mapToLong(it -> it.length)
                    .sum();
            while (maxSizePerUser > 0 && storedSize + data.getBytes().length > maxSizePerUser && storedSize > 0) {
                ZSetOperations.TypedTuple<String> evictedTuple = zSetOperations.popMin("userA:" + userID);
                if (evictedTuple == null || evictedTuple.getValue() == null)
                    throw new DataNotFoundException("User has stored " + storedSize + " bytes of data, but no record was found");
                hashOperations.delete("userD:" + userID, evictedTuple.getValue());
                storedSize -= evictedTuple.getValue().getBytes().length;
            }
        }
    }

    private void validateData(String userID, String ID, String data){
        if (userID == null || userID.length()>MAX_SIZE_PER_RECORD)
            throw new IllegalArgumentException("Illegal userID");

        if (ID == null || ID.length()>MAX_SIZE_PER_RECORD)
            throw new IllegalArgumentException("Illegal ID");

        if (data == null || data.length()>MAX_SIZE_PER_RECORD)
            throw new IllegalArgumentException("Illegal data");

    }
}