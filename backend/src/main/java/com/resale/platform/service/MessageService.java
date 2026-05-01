package com.resale.platform.service;

import com.resale.platform.entity.Message;

import java.util.List;
import java.util.Map;

public interface MessageService {

    List<Map<String, Object>> getMessageList(Long userId, Integer type);

    Map<String, Object> getUnreadCount(Long userId);

    void markAsRead(Long userId, Long id);

    void markAllAsRead(Long userId);

    void deleteMessage(Long userId, Long id);

    void sendNotification(Long senderId, Long receiverId, String title, String content, Integer type);

    void broadcastNotification(String title, String content, Integer type);
}
