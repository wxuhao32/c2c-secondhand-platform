package com.resale.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.resale.platform.dto.request.SendMessageRequest;
import com.resale.platform.dto.response.ChatMessageResponse;
import com.resale.platform.dto.response.ConversationResponse;
import com.resale.platform.entity.ChatConversation;
import com.resale.platform.entity.ChatMessage;
import com.resale.platform.entity.User;
import com.resale.platform.mapper.ChatConversationMapper;
import com.resale.platform.mapper.ChatMessageMapper;
import com.resale.platform.mapper.UserMapper;
import com.resale.platform.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatConversationMapper conversationMapper;
    private final ChatMessageMapper messageMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        ConversationResponse conv = getOrCreateConversation(senderId, request.getReceiverId());

        ChatMessage message = new ChatMessage();
        message.setConversationId(conv.getId());
        message.setSenderId(senderId);
        message.setReceiverId(request.getReceiverId());
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType() != null ? request.getMessageType() : 0);
        message.setStatus(0);
        message.setIsDeleted(0);
        messageMapper.insert(message);

        ChatConversation conversation = conversationMapper.selectById(conv.getId());
        if (conversation != null) {
            conversation.setLastMessage(request.getContent());
            conversation.setLastMessageTime(LocalDateTime.now());
            if (conversation.getUser1Id().equals(request.getReceiverId())) {
                conversationMapper.incrementUser1Unread(conv.getId(), request.getReceiverId());
            } else {
                conversationMapper.incrementUser2Unread(conv.getId(), request.getReceiverId());
            }
            conversationMapper.updateById(conversation);
        }

        return toMessageResponse(message);
    }

    @Override
    public List<ConversationResponse> getConversations(Long userId) {
        List<ChatConversation> conversations = conversationMapper.findByUserId(userId);
        List<ConversationResponse> responses = new ArrayList<>();

        for (ChatConversation conv : conversations) {
            Long otherUserId = conv.getUser1Id().equals(userId) ? conv.getUser2Id() : conv.getUser1Id();
            Integer unreadCount = conv.getUser1Id().equals(userId) ? conv.getUser1UnreadCount() : conv.getUser2UnreadCount();

            User otherUser = userMapper.selectById(otherUserId);

            ConversationResponse response = ConversationResponse.builder()
                    .id(conv.getId())
                    .conversationKey(conv.getConversationKey())
                    .otherUserId(otherUserId)
                    .otherUserName(otherUser != null ? (otherUser.getNickname() != null ? otherUser.getNickname() : otherUser.getUsername()) : "未知用户")
                    .otherUserAvatar(otherUser != null ? otherUser.getAvatar() : null)
                    .lastMessage(conv.getLastMessage())
                    .lastMessageTime(conv.getLastMessageTime())
                    .unreadCount(unreadCount != null ? unreadCount : 0)
                    .build();
            responses.add(response);
        }

        return responses;
    }

    @Override
    public List<ChatMessageResponse> getMessages(Long userId, Long conversationId, int page, int size) {
        int offset = (page - 1) * size;
        List<ChatMessage> messages = messageMapper.findByConversationId(conversationId, size, offset);

        List<ChatMessageResponse> responses = new ArrayList<>();
        for (ChatMessage msg : messages) {
            responses.add(toMessageResponse(msg));
        }

        return responses;
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long conversationId) {
        messageMapper.markAsReadByConversation(conversationId, userId);

        ChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null) {
            if (conversation.getUser1Id().equals(userId)) {
                conversationMapper.resetUser1Unread(conversationId, userId);
            } else {
                conversationMapper.resetUser2Unread(conversationId, userId);
            }
        }
    }

    @Override
    public int getUnreadCount(Long userId) {
        return messageMapper.countTotalUnread(userId);
    }

    @Override
    @Transactional
    public ConversationResponse getOrCreateConversation(Long user1Id, Long user2Id) {
        String key = user1Id < user2Id
                ? user1Id + "_" + user2Id
                : user2Id + "_" + user1Id;

        ChatConversation existing = conversationMapper.findByConversationKey(key);
        if (existing != null) {
            Long otherUserId = existing.getUser1Id().equals(user1Id) ? existing.getUser2Id() : existing.getUser1Id();
            User otherUser = userMapper.selectById(otherUserId);
            Integer unreadCount = existing.getUser1Id().equals(user1Id) ? existing.getUser1UnreadCount() : existing.getUser2UnreadCount();

            return ConversationResponse.builder()
                    .id(existing.getId())
                    .conversationKey(existing.getConversationKey())
                    .otherUserId(otherUserId)
                    .otherUserName(otherUser != null ? (otherUser.getNickname() != null ? otherUser.getNickname() : otherUser.getUsername()) : "未知用户")
                    .otherUserAvatar(otherUser != null ? otherUser.getAvatar() : null)
                    .lastMessage(existing.getLastMessage())
                    .lastMessageTime(existing.getLastMessageTime())
                    .unreadCount(unreadCount != null ? unreadCount : 0)
                    .build();
        }

        ChatConversation newConv = new ChatConversation();
        newConv.setConversationKey(key);
        newConv.setUser1Id(user1Id);
        newConv.setUser2Id(user2Id);
        newConv.setUser1UnreadCount(0);
        newConv.setUser2UnreadCount(0);
        newConv.setIsDeleted(0);
        conversationMapper.insert(newConv);

        User otherUser = userMapper.selectById(user2Id);
        return ConversationResponse.builder()
                .id(newConv.getId())
                .conversationKey(key)
                .otherUserId(user2Id)
                .otherUserName(otherUser != null ? (otherUser.getNickname() != null ? otherUser.getNickname() : otherUser.getUsername()) : "未知用户")
                .otherUserAvatar(otherUser != null ? otherUser.getAvatar() : null)
                .unreadCount(0)
                .build();
    }

    private ChatMessageResponse toMessageResponse(ChatMessage msg) {
        User sender = userMapper.selectById(msg.getSenderId());
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .conversationId(msg.getConversationId())
                .senderId(msg.getSenderId())
                .receiverId(msg.getReceiverId())
                .content(msg.getContent())
                .messageType(msg.getMessageType())
                .status(msg.getStatus())
                .createdAt(msg.getCreatedAt())
                .senderName(sender != null ? (sender.getNickname() != null ? sender.getNickname() : sender.getUsername()) : "未知用户")
                .senderAvatar(sender != null ? sender.getAvatar() : null)
                .build();
    }
}
