package com.resale.platform.service;

import com.resale.platform.dto.request.SendMessageRequest;
import com.resale.platform.dto.response.ChatMessageResponse;
import com.resale.platform.dto.response.ConversationResponse;

import java.util.List;

public interface ChatService {

    ChatMessageResponse sendMessage(Long senderId, SendMessageRequest request);

    List<ConversationResponse> getConversations(Long userId);

    List<ChatMessageResponse> getMessages(Long userId, Long conversationId, int page, int size);

    void markAsRead(Long userId, Long conversationId);

    int getUnreadCount(Long userId);

    ConversationResponse getOrCreateConversation(Long user1Id, Long user2Id);
}
