package com.resale.platform.controller;

import com.resale.platform.common.AuditLog;
import com.resale.platform.common.Result;
import com.resale.platform.security.SecurityUser;
import com.resale.platform.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Tag(name = "站内消息", description = "站内信查看、已读标记、删除等接口")
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    @Operation(summary = "获取消息列表", description = "获取当前用户的消息列表，可按类型筛选")
    public Result<List<Map<String, Object>>> getMessageList(
            @Parameter(description = "消息类型") @RequestParam(required = false) Integer type) {
        Long userId = getCurrentUserId();
        List<Map<String, Object>> messages = messageService.getMessageList(userId, type);
        return Result.success(messages);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读消息数", description = "获取当前用户的未读消息数量")
    public Result<Map<String, Object>> getUnreadCount() {
        Long userId = getCurrentUserId();
        Map<String, Object> count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记已读", description = "将指定消息标记为已读")
    public Result<Void> markAsRead(
            @Parameter(description = "消息ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        messageService.markAsRead(userId, id);
        return Result.success();
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读", description = "将所有消息标记为已读")
    public Result<Void> markAllAsRead() {
        Long userId = getCurrentUserId();
        messageService.markAllAsRead(userId);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除消息", description = "删除指定消息")
    @AuditLog(module = "消息", action = "删除", description = "用户删除站内消息")
    public Result<Void> deleteMessage(
            @Parameter(description = "消息ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        messageService.deleteMessage(userId, id);
        return Result.success();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}
