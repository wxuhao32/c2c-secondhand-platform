package com.resale.platform.service.impl;

import com.resale.platform.service.CacheService;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LocalCacheServiceImpl implements CacheService {

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        long expireAt = System.currentTimeMillis() + unit.toMillis(timeout);
        cache.put(key, new CacheEntry(value, expireAt));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return null;
        if (System.currentTimeMillis() > entry.expireAt) {
            cache.remove(key);
            return null;
        }
        return (T) entry.value;
    }

    @Override
    public void delete(String key) {
        cache.remove(key);
    }

    @Override
    public boolean hasKey(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) return false;
        if (System.currentTimeMillis() > entry.expireAt) {
            cache.remove(key);
            return false;
        }
        return true;
    }

    @Override
    public boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit) {
        if (hasKey(key)) return false;
        set(key, value, timeout, unit);
        return true;
    }

    @Override
    public void deleteByPattern(String pattern) {
        String regex = pattern.replace("*", ".*");
        cache.keySet().removeIf(key -> key.matches(regex));
    }

    private record CacheEntry(Object value, long expireAt) {}
}
