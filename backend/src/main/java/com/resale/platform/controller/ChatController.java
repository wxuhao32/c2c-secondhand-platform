package com.resale.platform.controller;

import com.resale.platform.common.Result;
import com.resale.platform.dto.request.SendMessageRequest;
import com.resale.platform.dto.response.ChatMessageResponse;
import com.resale.platform.dto.response.ConversationResponse;
import com.resale.platform.security.SecurityUser;
import com.resale.platform.service.ChatService;
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
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public Result<ChatMessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        Long userId = getCurrentUserId();
        ChatMessageResponse response = chatService.sendMessage(userId, request);
        return Result.success(response);
    }

    @GetMapping("/conversations")
    public Result<List<ConversationResponse>> getConversations() {
        Long userId = getCurrentUserId();
        List<ConversationResponse> conversations = chatService.getConversations(userId);
        return Result.success(conversations);
    }

    @GetMapping("/messages/{conversationId}")
    public Result<List<ChatMessageResponse>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getCurrentUserId();
        List<ChatMessageResponse> messages = chatService.getMessages(userId, conversationId, page, size);
        return Result.success(messages);
    }

    @PutMapping("/read/{conversationId}")
    public Result<Void> markAsRead(@PathVariable Long conversationId) {
        Long userId = getCurrentUserId();
        chatService.markAsRead(userId, conversationId);
        return Result.success();
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> getUnreadCount() {
        Long userId = getCurrentUserId();
        int count = chatService.getUnreadCount(userId);
        return Result.success(Map.of("count", count));
    }

    @PostMapping("/conversation")
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
