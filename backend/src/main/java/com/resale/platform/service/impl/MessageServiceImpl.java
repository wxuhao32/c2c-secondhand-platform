package com.resale.platform.service.impl;

import com.resale.platform.common.BusinessException;
import com.resale.platform.common.ExceptionEnum;
import com.resale.platform.entity.Message;
import com.resale.platform.entity.User;
import com.resale.platform.mapper.MessageMapper;
import com.resale.platform.mapper.UserMapper;
import com.resale.platform.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final UserMapper userMapper;

    @Override
    public List<Map<String, Object>> getMessageList(Long userId, Integer type) {
        List<Message> messages;
        if (type != null) {
            messages = messageMapper.findByReceiverIdAndType(userId, type);
        } else {
            messages = messageMapper.findByReceiverId(userId);
        }
        return messages.stream().map(this::messageToMap).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getUnreadCount(Long userId) {
        int unreadCount = messageMapper.countUnread(userId);
        return Map.of("unreadCount", unreadCount);
    }

    @Override
    public void markAsRead(Long userId, Long id) {
        messageMapper.markAsRead(id, userId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        messageMapper.markAllAsRead(userId);
    }

    @Override
    public void deleteMessage(Long userId, Long id) {
        Message message = messageMapper.selectById(id);
        if (message == null || !message.getReceiverId().equals(userId)) {
            throw new BusinessException(ExceptionEnum.FORBIDDEN);
        }
        message.setIsDeleted(1);
        messageMapper.updateById(message);
    }

    @Override
    @Transactional
    public void sendNotification(Long senderId, Long receiverId, String title, String content, Integer type) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setType(type);
        message.setTitle(title);
        message.setContent(content);
        message.setIsRead(0);
        message.setIsDeleted(0);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);
    }

    @Override
    @Transactional
    public void broadcastNotification(String title, String content, Integer type) {
        List<User> allUsers = userMapper.selectList(null);
        for (User user : allUsers) {
            if (user.getUserId() == null) continue;
            Message message = new Message();
            message.setSenderId(null);
            message.setReceiverId(user.getUserId());
            message.setType(type);
            message.setTitle(title);
            message.setContent(content);
            message.setIsRead(0);
            message.setIsDeleted(0);
            message.setCreatedAt(LocalDateTime.now());
            messageMapper.insert(message);
        }
    }

    private Map<String, Object> messageToMap(Message message) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", message.getId());
        map.put("senderId", message.getSenderId());
        map.put("receiverId", message.getReceiverId());
        map.put("type", message.getType());
        map.put("title", message.getTitle());
        map.put("content", message.getContent());
        map.put("isRead", message.getIsRead());
        map.put("createdAt", message.getCreatedAt());
        map.put("typeName", message.getType() == 0 ? "系统通知" : "用户消息");
        return map;
    }
}
