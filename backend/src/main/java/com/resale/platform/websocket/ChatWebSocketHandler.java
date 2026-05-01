package com.resale.platform.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resale.platform.entity.ChatMessage;
import com.resale.platform.mapper.ChatMessageMapper;
import com.resale.platform.mapper.UserMapper;
import com.resale.platform.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;

    private static final Map<Long, WebSocketSession> onlineSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long userId = extractUserId(session);
        if (userId != null) {
            onlineSessions.put(userId, session);
            log.info("WebSocket连接建立: userId={}, 当前在线人数={}", userId, onlineSessions.size());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Long userId = extractUserId(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }

        try {
            Map<String, Object> messageData = objectMapper.readValue(textMessage.getPayload(), Map.class);
            String type = (String) messageData.get("type");

            if ("CHAT".equals(type)) {
                handleChatMessage(userId, messageData);
            } else if ("READ".equals(type)) {
                handleReadMessage(userId, messageData);
            } else if ("PING".equals(type)) {
                session.sendMessage(new TextMessage("{\"type\":\"PONG\"}"));
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: userId={}, error={}", userId, e.getMessage());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                    Map.of("type", "ERROR", "message", "消息处理失败")
            )));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = extractUserId(session);
        if (userId != null) {
            onlineSessions.remove(userId);
            log.info("WebSocket连接关闭: userId={}, status={}, 当前在线人数={}", userId, status, onlineSessions.size());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        Long userId = extractUserId(session);
        log.error("WebSocket传输错误: userId={}, error={}", userId, exception.getMessage());
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    private void handleChatMessage(Long senderId, Map<String, Object> messageData) throws IOException {
        Long receiverId = Long.valueOf(messageData.get("receiverId").toString());
        String content = (String) messageData.get("content");
        Integer messageType = messageData.containsKey("messageType")
                ? Integer.valueOf(messageData.get("messageType").toString()) : 0;

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(senderId);
        chatMessage.setReceiverId(receiverId);
        chatMessage.setContent(content);
        chatMessage.setMessageType(messageType);
        chatMessage.setStatus(0);
        chatMessage.setIsDeleted(0);

        chatMessageMapper.insert(chatMessage);

        Map<String, Object> responseMsg = Map.of(
                "type", "CHAT",
                "id", chatMessage.getId(),
                "senderId", senderId,
                "receiverId", receiverId,
                "content", content,
                "messageType", messageType,
                "createdAt", chatMessage.getCreatedAt().toString()
        );

        String responseJson = objectMapper.writeValueAsString(responseMsg);

        WebSocketSession receiverSession = onlineSessions.get(receiverId);
        if (receiverSession != null && receiverSession.isOpen()) {
            receiverSession.sendMessage(new TextMessage(responseJson));
        }

        WebSocketSession senderSession = onlineSessions.get(senderId);
        if (senderSession != null && senderSession.isOpen()) {
            senderSession.sendMessage(new TextMessage(responseJson));
        }
    }

    private void handleReadMessage(Long userId, Map<String, Object> messageData) throws IOException {
        Long conversationId = Long.valueOf(messageData.get("conversationId").toString());
        chatMessageMapper.markAsReadByConversation(conversationId, userId);

        Map<String, Object> responseMsg = Map.of(
                "type", "READ",
                "conversationId", conversationId,
                "userId", userId
        );

        WebSocketSession session = onlineSessions.get(userId);
        if (session != null && session.isOpen()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(responseMsg)));
        }
    }

    public boolean isUserOnline(Long userId) {
        WebSocketSession session = onlineSessions.get(userId);
        return session != null && session.isOpen();
    }

    public int getOnlineCount() {
        return onlineSessions.size();
    }

    private Long extractUserId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) return null;

        String query = uri.getQuery();
        if (query == null) return null;

        for (String param : query.split("&")) {
            String[] kv = param.split("=");
            if (kv.length == 2 && "token".equals(kv[0])) {
                try {
                    String token = kv[1];
                    if (jwtTokenProvider.validateToken(token)) {
                        return jwtTokenProvider.getUserIdFromToken(token);
                    }
                } catch (Exception e) {
                    log.warn("解析WebSocket token失败: {}", e.getMessage());
                }
            }
        }
        return null;
    }
}
