package com.resale.platform.common;

import com.resale.platform.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLock {

    private final CacheService cacheService;

    private static final String LOCK_PREFIX = "lock:";
    private static final long DEFAULT_TIMEOUT = 30;
    private static final long DEFAULT_WAIT_INTERVAL = 100;
    private static final long DEFAULT_MAX_WAIT = 10000;

    public boolean tryLock(String key, long timeout, TimeUnit unit) {
        String lockKey = LOCK_PREFIX + key;
        return cacheService.setIfAbsent(lockKey, Thread.currentThread().getId(), timeout, unit);
    }

    public boolean tryLock(String key) {
        return tryLock(key, DEFAULT_TIMEOUT, TimeUnit.SECONDS);
    }

    public void unlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        cacheService.delete(lockKey);
    }

    public <T> T executeWithLock(String key, long timeout, TimeUnit unit, Supplier<T> supplier) {
        boolean locked = false;
        try {
            locked = tryLockWithWait(key, timeout, unit);
            if (!locked) {
                throw new BusinessException(ExceptionEnum.OPERATION_TOO_FREQUENT);
            }
            return supplier.get();
        } finally {
            if (locked) {
                unlock(key);
            }
        }
    }

    public void executeWithLock(String key, Runnable runnable) {
        executeWithLock(key, DEFAULT_TIMEOUT, TimeUnit.SECONDS, () -> {
            runnable.run();
            return null;
        });
    }

    public <T> T executeWithLock(String key, Supplier<T> supplier) {
        return executeWithLock(key, DEFAULT_TIMEOUT, TimeUnit.SECONDS, supplier);
    }

    private boolean tryLockWithWait(String key, long timeout, TimeUnit unit) {
        long start = System.currentTimeMillis();
        long maxWaitMillis = DEFAULT_MAX_WAIT;
        while (System.currentTimeMillis() - start < maxWaitMillis) {
            if (tryLock(key, timeout, unit)) {
                return true;
            }
            try {
                Thread.sleep(DEFAULT_WAIT_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return false;
    }
}
