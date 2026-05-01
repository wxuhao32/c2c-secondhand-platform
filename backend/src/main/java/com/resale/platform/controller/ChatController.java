package com.resale.platform.controller;

import com.resale.platform.common.Result;
import com.resale.platform.dto.request.SendMessageRequest;
import com.resale.platform.dto.response.ChatMessageResponse;
import com.resale.platform.dto.response.ConversationResponse;
import com.resale.platform.security.SecurityUser;
import com.resale.platform.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "实时聊天", description = "聊天会话、消息发送、未读计数等接口")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    @Operation(summary = "发送消息", description = "向指定会话发送聊天消息")
    public Result<ChatMessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        Long userId = getCurrentUserId();
        ChatMessageResponse response = chatService.sendMessage(userId, request);
        return Result.success(response);
    }

    @GetMapping("/conversations")
    @Operation(summary = "获取会话列表", description = "获取当前用户的所有聊天会话")
    public Result<List<ConversationResponse>> getConversations() {
        Long userId = getCurrentUserId();
        List<ConversationResponse> conversations = chatService.getConversations(userId);
        return Result.success(conversations);
    }

    @GetMapping("/messages/{conversationId}")
    @Operation(summary = "获取聊天记录", description = "获取指定会话的聊天消息，支持分页")
    public Result<List<ChatMessageResponse>> getMessages(
            @Parameter(description = "会话ID") @PathVariable Long conversationId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId();
        List<ChatMessageResponse> messages = chatService.getMessages(userId, conversationId, page, size);
        return Result.success(messages);
    }

    @PutMapping("/read/{conversationId}")
    @Operation(summary = "标记已读", description = "将指定会话的消息标记为已读")
    public Result<Void> markAsRead(
            @Parameter(description = "会话ID") @PathVariable Long conversationId) {
        Long userId = getCurrentUserId();
        chatService.markAsRead(userId, conversationId);
        return Result.success();
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读消息数", description = "获取当前用户的聊天未读消息数量")
    public Result<Map<String, Integer>> getUnreadCount() {
        Long userId = getCurrentUserId();
        int count = chatService.getUnreadCount(userId);
        return Result.success(Map.of("count", count));
    }

    @PostMapping("/conversation")
    @Operation(summary = "创建/获取会话", description = "与指定用户创建或获取聊天会话")
    public Result<ConversationResponse> getOrCreateConversation(@RequestBody Map<String, Long> body) {
        Long userId = getCurrentUserId();
        Long otherUserId = body.get("otherUserId");
        if (otherUserId == null) {
            return Result.error("对方用户ID不能为空");
        }
        ConversationResponse conversation = chatService.getOrCreateConversation(userId, otherUserId);
        return Result.success(conversation);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}
