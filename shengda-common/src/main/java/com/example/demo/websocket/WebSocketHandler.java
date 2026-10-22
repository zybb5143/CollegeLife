package com.example.demo.websocket;

import com.example.demo.mapper.PrivateMessageMapper;
import com.example.demo.model.PrivateMessage;
import com.example.demo.service.PrivateMessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 消息处理器
 * 数据库为唯一数据源，消息发送时立即落库，不再使用内存缓存。
 */
@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private PrivateMessageMapper privateMessageMapper;

    @Autowired
    private PrivateMessageService privateMessageService;

    // 在线用户会话缓存：一个用户名对应多个 session，支持多标签页/多设备同时在线
    private static final Map<String, Set<WebSocketSession>> userSessionMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 连接建立时，将用户信息存入 Map
        String userName = getUserNameFromSession(session);
        if (userName != null) {
            userSessionMap.computeIfAbsent(userName, k -> ConcurrentHashMap.newKeySet()).add(session);
            logger.info("用户 {} 已连接 WebSocket，当前在线用户数：{}", userName, userSessionMap.size());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String userName = getUserNameFromSession(session);

        if (userName == null) {
            logger.warn("收到消息但无法获取用户名");
            return;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            String type = jsonNode.path("type").asText(null);

            if (type == null) {
                logger.warn("收到缺少 type 字段的消息: {}", payload);
                return;
            }

            switch (type) {
                case "sendMessage":
                    handleSendMessage(userName, jsonNode, session);
                    break;
                case "getHistory":
                    handleGetHistory(userName, jsonNode, session);
                    break;
                default:
                    logger.warn("未知的消息类型: {}", type);
            }
        } catch (Exception e) {
            logger.error("处理 WebSocket 消息失败", e);
        }
    }

    /**
     * 处理发送消息
     */
    private void handleSendMessage(String senderName, JsonNode jsonNode, WebSocketSession session) {
        // 参数校验，避免缺失字段导致 NPE 静默失败
        String receiverName = jsonNode.path("receiverName").asText(null);
        String content = jsonNode.path("messageContent").asText(null);
        if (receiverName == null || receiverName.trim().isEmpty()
                || content == null || content.trim().isEmpty()) {
            sendError(session, "接收者或消息内容不能为空");
            return;
        }

        String messageType = jsonNode.has("messageType") ? jsonNode.path("messageType").asText("text") : "text";
        Integer productId = jsonNode.has("productId") && !jsonNode.get("productId").isNull()
                ? jsonNode.get("productId").asInt() : null;

        // 创建消息对象
        PrivateMessage message = new PrivateMessage();
        message.setSenderName(senderName);
        message.setReceiverName(receiverName);
        message.setMessageContent(content);
        message.setMessageType(messageType);
        message.setProductId(productId);
        message.setIsRead(0);
        message.setSendTime(new Timestamp(System.currentTimeMillis()));

        // 立即写入数据库（唯一数据源，保证红点计数与消息持久化）
        // 走 Service 层：落库的同时通过 RabbitMQ 投递私信提醒
        boolean saved = privateMessageService.sendMessage(message);
        if (!saved) {
            sendError(session, "消息发送失败，请稍后重试");
            return;
        }
        logger.info("用户 {} 发送消息给 {}，已保存到数据库(messageId={})", senderName, receiverName, message.getMessageId());

        // 实时推送给接收者的所有在线会话
        boolean pushed = pushToUser(receiverName, message, false);
        if (!pushed) {
            logger.info("用户 {} 不在线，消息将在其上线后通过历史记录获取", receiverName);
        }

        // 返回发送成功给发送者
        try {
            Map<String, Object> msgForSender = objectMapper.convertValue(message, Map.class);
            msgForSender.put("isSender", true);

            Map<String, Object> response = new HashMap<>();
            response.put("type", "sendSuccess");
            response.put("message", msgForSender);
            sendTextSafely(session, objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            logger.error("返回发送结果失败", e);
        }
    }

    /**
     * 处理获取历史记录
     */
    private void handleGetHistory(String currentUserName, JsonNode jsonNode, WebSocketSession session) {
        String targetUser = jsonNode.path("targetUser").asText(null);
        if (targetUser == null || targetUser.trim().isEmpty()) {
            sendError(session, "targetUser 不能为空");
            return;
        }

        try {
            // 数据库为唯一数据源，直接查询已落库的历史消息（按时间正序）
            List<PrivateMessage> dbMessages = privateMessageMapper.getConversation(currentUserName, targetUser);

            // 为每条消息添加 isSender 标记
            List<Map<String, Object>> messagesWithDirection = new ArrayList<>();
            for (PrivateMessage msg : dbMessages) {
                Map<String, Object> msgMap = objectMapper.convertValue(msg, Map.class);
                msgMap.put("isSender", msg.getSenderName().equals(currentUserName));
                messagesWithDirection.add(msgMap);
            }

            // 标记对方发来的消息为已读
            privateMessageMapper.markAllAsRead(targetUser, currentUserName);

            // 通过发起请求的会话返回结果（多端场景下避免发错 session）
            Map<String, Object> response = new HashMap<>();
            response.put("type", "historyData");
            response.put("targetUser", targetUser);
            response.put("messages", messagesWithDirection);

            sendTextSafely(session, objectMapper.writeValueAsString(response));

            logger.info("向用户 {} 返回与 {} 的 {} 条历史记录", currentUserName, targetUser, dbMessages.size());
        } catch (Exception e) {
            logger.error("获取历史记录失败", e);
        }
    }

    /**
     * 向指定用户的所有在线会话推送消息
     *
     * @param isSender 对接收方而言恒为 false
     * @return 是否至少成功推送给一个在线会话
     */
    private boolean pushToUser(String userName, PrivateMessage message, boolean isSender) {
        Set<WebSocketSession> sessions = userSessionMap.get(userName);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }

        boolean pushed = false;
        try {
            Map<String, Object> msgMap = objectMapper.convertValue(message, Map.class);
            msgMap.put("isSender", isSender);
            // 显式标注消息类型，前端可稳定识别（字段仍平铺，兼容 senderName 等读取）
            msgMap.put("type", "receiveMessage");
            String payload = objectMapper.writeValueAsString(msgMap);

            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    sendTextSafely(s, payload);
                    pushed = true;
                }
            }
        } catch (Exception e) {
            logger.error("构造推送消息失败", e);
        }
        return pushed;
    }

    /**
     * 向指定会话回传错误提示
     */
    private void sendError(WebSocketSession session, String errorMessage) {
        if (session == null) {
            return;
        }
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("type", "error");
            response.put("message", errorMessage);
            sendTextSafely(session, objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            logger.error("发送错误响应失败", e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket 传输错误", exception);
        if (session.isOpen()) {
            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userName = getUserNameFromSession(session);
        if (userName != null) {
            // 只移除当前关闭的会话，不影响该用户其它仍在线的会话（修复多标签页误删问题）
            Set<WebSocketSession> sessions = userSessionMap.get(userName);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessionMap.remove(userName, sessions);
                }
            }
            logger.info("用户 {} 断开一个 WebSocket 连接，当前在线用户数：{}", userName, userSessionMap.size());
        }
    }

    /**
     * 检查用户是否在线（任一会话处于打开状态即视为在线）
     */
    public boolean isUserOnline(String userName) {
        Set<WebSocketSession> sessions = userSessionMap.get(userName);
        if (sessions == null || sessions.isEmpty()) {
            return false;
        }
        return sessions.stream().anyMatch(WebSocketSession::isOpen);
    }

    /**
     * 向指定用户的所有在线会话推送一条自定义通知（供 MQ 消费者调用，实现前端实时提醒）
     *
     * @param userName 接收者用户名
     * @param payload  已含 type 等字段的消息体
     */
    public void pushNotification(String userName, Map<String, Object> payload) {
        Set<WebSocketSession> sessions = userSessionMap.get(userName);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        try {
            String text = objectMapper.writeValueAsString(payload);
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    sendTextSafely(s, text);
                }
            }
        } catch (Exception e) {
            logger.error("构造提醒消息失败", e);
        }
    }

    /**
     * 线程安全地向单个会话发送文本消息。
     * Tomcat 的 WebSocketSession.sendMessage 并非线程安全，多线程并发写同一会话
     * （如 WS 推送与 MQ 提醒推送同时到达）会抛 IllegalStateException(TEXT_PARTIAL_WRITING)，
     * 因此按 session 加锁，串行化对同一连接的写入。
     */
    private void sendTextSafely(WebSocketSession session, String payload) {
        if (session == null) {
            return;
        }
        synchronized (session) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(payload));
                } catch (IOException e) {
                    logger.error("发送 WebSocket 消息失败", e);
                }
            }
        }
    }

    /**
     * 从 Session 中获取用户名
     */
    private String getUserNameFromSession(WebSocketSession session) {
        Map<String, Object> attributes = session.getAttributes();
        return (String) attributes.get("userName");
    }
}
