package com.resale.platform.service;

import com.resale.platform.service.impl.LocalCacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("本地缓存服务单元测试")
class LocalCacheServiceTest {

    private LocalCacheServiceImpl cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new LocalCacheServiceImpl();
    }

    @Test
    @DisplayName("设置和获取缓存")
    void setAndGet() {
        cacheService.set("key1", "value1", 60, TimeUnit.SECONDS);
        String result = cacheService.get("key1", String.class);
        assertEquals("value1", result);
    }

    @Test
    @DisplayName("缓存过期后返回null")
    void expiredReturnsNull() throws InterruptedException {
        cacheService.set("key1", "value1", 1, TimeUnit.SECONDS);
        Thread.sleep(1100);
        String result = cacheService.get("key1", String.class);
        assertNull(result);
    }

    @Test
    @DisplayName("删除缓存")
    void delete() {
        cacheService.set("key1", "value1", 60, TimeUnit.SECONDS);
        cacheService.delete("key1");
        assertFalse(cacheService.hasKey("key1"));
    }

    @Test
    @DisplayName("检查缓存是否存在")
    void hasKey() {
        cacheService.set("key1", "value1", 60, TimeUnit.SECONDS);
        assertTrue(cacheService.hasKey("key1"));
        assertFalse(cacheService.hasKey("key2"));
    }

    @Test
    @DisplayName("setIfAbsent - 首次设置成功")
    void setIfAbsent_success() {
        boolean result = cacheService.setIfAbsent("lock1", "owner1", 30, TimeUnit.SECONDS);
        assertTrue(result);
    }

    @Test
    @DisplayName("setIfAbsent - 已存在返回false")
    void setIfAbsent_alreadyExists() {
        cacheService.setIfAbsent("lock1", "owner1", 30, TimeUnit.SECONDS);
        boolean result = cacheService.setIfAbsent("lock1", "owner2", 30, TimeUnit.SECONDS);
        assertFalse(result);
    }

    @Test
    @DisplayName("按模式删除缓存")
    void deleteByPattern() {
        cacheService.set("product:1", "data1", 60, TimeUnit.SECONDS);
        cacheService.set("product:2", "data2", 60, TimeUnit.SECONDS);
        cacheService.set("order:1", "data3", 60, TimeUnit.SECONDS);

        cacheService.deleteByPattern("product:*");

        assertFalse(cacheService.hasKey("product:1"));
        assertFalse(cacheService.hasKey("product:2"));
        assertTrue(cacheService.hasKey("order:1"));
    }
}
