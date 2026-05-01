package com.resale.platform.service;

import java.util.concurrent.TimeUnit;

public interface CacheService {

    void set(String key, Object value, long timeout, TimeUnit unit);

    <T> T get(String key, Class<T> clazz);

    void delete(String key);

    boolean hasKey(String key);

    boolean setIfAbsent(String key, Object value, long timeout, TimeUnit unit);

    void deleteByPattern(String pattern);
}
