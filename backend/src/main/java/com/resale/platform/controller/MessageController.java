package com.resale.platform.controller;

import com.resale.platform.common.Result;
import com.resale.platform.entity.Message;
import com.resale.platform.mapper.MessageMapper;
import com.resale.platform.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageMapper messageMapper;

    @GetMapping
    public Result<List<Map<String, Object>>> getMessageList(
            @RequestParam(required = false) Integer type) {
        Long userId = getCurrentUserId();
        List<Message> messages;
        if (type != null) {
            messages = messageMapper.findByReceiverIdAndType(userId, type);
        } else {
            messages = messageMapper.findByReceiverId(userId);
        }
        List<Map<String, Object>> result = messages.stream().map(this::messageToMap).collect(Collectors.toList());
        return Result.success(result);
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Object>> getUnreadCount() {
        Long userId = getCurrentUserId();
        int unreadCount = messageMapper.countUnread(userId);
        return Result.success(Map.of("unreadCount", unreadCount));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        messageMapper.markAsRead(id, userId);
        return Result.success();
    }

    @PutMapping("/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = getCurrentUserId();
        messageMapper.markAllAsRead(userId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Message message = messageMapper.selectById(id);
        if (message == null || !message.getReceiverId().equals(userId)) {
            return Result.error(403, "无权操作");
        }
        message.setIsDeleted(1);
        messageMapper.updateById(message);
        return Result.success();
    }

    @PostMapping("/system")
    public Result<Void> sendSystemMessage(@RequestBody Map<String, Object> body) {
        Long receiverId = Long.valueOf(body.get("receiverId").toString());
        String title = (String) body.getOrDefault("title", "系统通知");
        String content = (String) body.get("content");
        Integer type = body.get("type") != null ? Integer.valueOf(body.get("type").toString()) : 0;

        Message message = new Message();
        message.setSenderId(null);
        message.setReceiverId(receiverId);
        message.setType(type);
        message.setTitle(title);
        message.setContent(content);
        message.setIsRead(0);
        message.setIsDeleted(0);
        message.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(message);
        return Result.success();
    }

    @PostMapping("/broadcast")
    public Result<Void> broadcastMessage(@RequestBody Map<String, Object> body) {
        return Result.success();
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

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}
