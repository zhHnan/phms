package com.phms.ws;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phms.entity.SupportChatMessage;
import com.phms.service.SupportChatMessageService;
import com.phms.service.SupportMatchService;
import com.phms.service.StaffService;
import com.phms.entity.Staff;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SupportWebSocketHandler extends TextWebSocketHandler {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int HISTORY_LIMIT = 50;

    private final SupportMatchService supportMatchService;
    private final StaffService staffService;
    private final SupportChatMessageService chatMessageService;

    private final Map<Long, WebSocketSession> staffSessions = new ConcurrentHashMap<>();
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<Long, Long> userToStaff = new ConcurrentHashMap<>();
    private final Map<Long, Long> staffToUser = new ConcurrentHashMap<>();
    private final Map<Long, Long> userRoomMap = new ConcurrentHashMap<>();
    private final Map<Long, Long> userHotelMap = new ConcurrentHashMap<>();
    private final Map<String, String> sessionRole = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionIdMap = new ConcurrentHashMap<>();

    public SupportWebSocketHandler(SupportMatchService supportMatchService, StaffService staffService,
            SupportChatMessageService chatMessageService) {
        this.supportMatchService = supportMatchService;
        this.staffService = staffService;
        this.chatMessageService = chatMessageService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null)
            return;
        Map<String, String> params = QueryUtil.parse(uri.getQuery());
        String role = params.getOrDefault("role", "user");

        // 兼容新的参数格式：staffId + roomId + token (前端用户聊天)
        if (params.containsKey("staffId") && !params.containsKey("role")) {
            // 前端用户连接客服
            String token = params.get("token");
            Long userId = extractUserIdFromToken(token);
            Long staffId = parseLong(params.get("staffId"));
            Long roomId = parseLong(params.get("roomId"));
            Long hotelId = parseLong(params.get("hotelId"));

            if (userId != null && staffId != null) {
                userSessions.put(userId, session);
                sessionRole.put(session.getId(), "user");
                sessionIdMap.put(session.getId(), userId);
                userToStaff.put(userId, staffId);
                staffToUser.put(staffId, userId);
                if (roomId != null)
                    userRoomMap.put(userId, roomId);
                if (hotelId != null)
                    userHotelMap.put(userId, hotelId);

                // 通知员工端有新用户接入
                WebSocketSession staffSession = staffSessions.get(staffId);
                if (staffSession != null && staffSession.isOpen()) {
                    staffSession.sendMessage(new TextMessage("{\"type\":\"USER_ASSIGNED\",\"userId\":" + userId
                            + ",\"roomId\":" + (roomId != null ? roomId : "null")
                            + ",\"hotelId\":" + (hotelId != null ? hotelId : "null") + "}"));
                }

                notifyStaffConnected(userId, staffId, session);
                sendHistoryToUser(userId, staffId, session);
                sendHistoryToStaff(userId, staffId);
            }
            return;
        }

        if ("staff".equalsIgnoreCase(role)) {
            Long staffId = parseLong(params.get("staffId"));
            Long targetUserId = parseLong(params.get("userId")); // staff 主动连接某个用户
            if (staffId != null) {
                staffSessions.put(staffId, session);
                sessionRole.put(session.getId(), "staff");
                sessionIdMap.put(session.getId(), staffId);

                // 如果指定了userId，建立staff到user的映射
                if (targetUserId != null) {
                    userToStaff.put(targetUserId, staffId);
                    staffToUser.put(staffId, targetUserId);
                    // 通知用户端员工已接入
                    WebSocketSession userSession = userSessions.get(targetUserId);
                    if (userSession != null && userSession.isOpen()) {
                        String staffName = getStaffName(staffId);
                        userSession.sendMessage(
                                new TextMessage("{\"type\":\"STAFF_CONNECTED\",\"staffId\":" + staffId
                                        + ",\"staffName\":\"" + staffName + "\"}"));
                    }
                    sendHistoryToStaff(targetUserId, staffId);
                }
            }
        } else {
            Long userId = parseLong(params.get("userId"));
            Long hotelId = parseLong(params.get("hotelId"));
            Long roomId = parseLong(params.get("roomId"));
            if (userId != null) {
                userSessions.put(userId, session);
                sessionRole.put(session.getId(), "user");
                sessionIdMap.put(session.getId(), userId);
                if (roomId != null)
                    userRoomMap.put(userId, roomId);
                if (hotelId != null)
                    userHotelMap.put(userId, hotelId);
                Long staffId = supportMatchService.matchOnDutyStaff(hotelId, LocalDateTime.now());
                if (staffId != null) {
                    userToStaff.put(userId, staffId);
                    staffToUser.put(staffId, userId);
                    session.sendMessage(new TextMessage("ASSIGNED:" + staffId));
                    notifyStaffConnected(userId, staffId, session);
                    sendHistoryToUser(userId, staffId, session);
                    sendHistoryToStaff(userId, staffId);
                    WebSocketSession staffSession = staffSessions.get(staffId);
                    if (staffSession != null && staffSession.isOpen()) {
                        staffSession.sendMessage(new TextMessage("{\"type\":\"USER_ASSIGNED\",\"userId\":" + userId
                                + ",\"roomId\":" + (roomId != null ? roomId : "null")
                                + ",\"hotelId\":" + (hotelId != null ? hotelId : "null") + "}"));
                    }
                } else {
                    session.sendMessage(new TextMessage("NO_STAFF"));
                }
            }
        }
    }

    private Long extractUserIdFromToken(String token) {
        // 从 token 解析用户ID
        try {
            if (token == null || token.isBlank())
                return null;
            Object loginId = StpUtil.getLoginIdByToken(token);
            if (loginId == null)
                return null;
            return Long.valueOf(loginId.toString());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, @NotNull TextMessage message) throws Exception {
        String role = sessionRole.get(session.getId());
        Long id = sessionIdMap.get(session.getId());
        if (role == null || id == null)
            return;
        String content = extractContent(message.getPayload());
        if ("user".equals(role)) {
            Long staffId = userToStaff.get(id);
            if (staffId == null)
                return;
            WebSocketSession staffSession = staffSessions.get(staffId);
            if (staffSession != null && staffSession.isOpen()) {
                staffSession.sendMessage(new TextMessage(message.getPayload()));
            }
            if (content != null && !content.isBlank()) {
                chatMessageService.saveMessage(id, staffId, "user", content, userRoomMap.get(id),
                        userHotelMap.get(id));
            }
        } else {
            // staff -> user
            Long staffId = id;
            Long userId = resolveUserIdForStaff(staffId);
            if (userId == null)
                return;
            WebSocketSession userSession = userSessions.get(userId);
            if (userSession != null && userSession.isOpen()) {
                userSession.sendMessage(new TextMessage(message.getPayload()));
            }
            if (content != null && !content.isBlank()) {
                chatMessageService.saveMessage(userId, staffId, "staff", content, userRoomMap.get(userId),
                        userHotelMap.get(userId));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) {
        String role = sessionRole.remove(session.getId());
        Long id = sessionIdMap.remove(session.getId());
        if (role == null || id == null)
            return;
        if ("staff".equals(role)) {
            staffSessions.remove(id);
            staffToUser.remove(id);
        } else {
            userSessions.remove(id);
            userToStaff.remove(id);
            userRoomMap.remove(id);
            userHotelMap.remove(id);
            staffToUser.entrySet().removeIf(e -> e.getValue().equals(id));
        }
    }

    private Long parseLong(String value) {
        try {
            return value == null ? null : Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Map<String, Object>> getConnectedUsers() {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Map.Entry<Long, WebSocketSession> entry : userSessions.entrySet()) {
            if (entry.getValue().isOpen()) {
                Map<String, Object> user = new HashMap<>();
                user.put("userId", entry.getKey());
                user.put("userName", "用户" + entry.getKey());
                user.put("unread", 0);
                if (userRoomMap.containsKey(entry.getKey())) {
                    user.put("roomId", userRoomMap.get(entry.getKey()));
                }
                if (userHotelMap.containsKey(entry.getKey())) {
                    user.put("hotelId", userHotelMap.get(entry.getKey()));
                }
                Long staffId = userToStaff.get(entry.getKey());
                if (staffId != null) {
                    user.put("assignedStaffId", staffId);
                }
                result.add(user);
            }
        }
        return result;
    }

    private void sendHistoryToUser(Long userId, Long staffId, WebSocketSession session) {
        if (userId == null || staffId == null || session == null || !session.isOpen()) {
            return;
        }
        try {
            List<SupportChatMessage> history = chatMessageService.listHistory(userId, staffId, HISTORY_LIMIT);
            String staffName = getStaffName(staffId);
            if (history.isEmpty()) {
                sendGreeting(staffName, session, staffId);
                return;
            }
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "HISTORY");
            payload.put("staffName", staffName);
            payload.put("messages", toPayloadMessages(history));
            session.sendMessage(new TextMessage(MAPPER.writeValueAsString(payload)));
        } catch (Exception ignored) {
        }
    }

    private void sendHistoryToStaff(Long userId, Long staffId) {
        if (userId == null || staffId == null) {
            return;
        }
        WebSocketSession staffSession = staffSessions.get(staffId);
        if (staffSession == null || !staffSession.isOpen()) {
            return;
        }
        try {
            List<SupportChatMessage> history = chatMessageService.listHistory(userId, staffId, HISTORY_LIMIT);
            if (history.isEmpty()) {
                return;
            }
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "HISTORY");
            payload.put("userId", userId);
            payload.put("messages", toPayloadMessages(history));
            staffSession.sendMessage(new TextMessage(MAPPER.writeValueAsString(payload)));
        } catch (Exception ignored) {
        }
    }

    private List<Map<String, Object>> toPayloadMessages(List<SupportChatMessage> history) {
        List<Map<String, Object>> msgs = new ArrayList<>();
        for (SupportChatMessage msg : history) {
            Map<String, Object> m = new HashMap<>();
            m.put("sender", msg.getSender());
            m.put("text", msg.getContent());
            m.put("ts", toEpochMilli(msg.getCreatedAt()));
            msgs.add(m);
        }
        return msgs;
    }

    private long toEpochMilli(LocalDateTime time) {
        if (time == null) {
            return System.currentTimeMillis();
        }
        return time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private void sendGreeting(String staffName, WebSocketSession session, Long staffId) {
        if (session == null || !session.isOpen()) {
            return;
        }
        try {
            String name = staffName == null ? "" : staffName;
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "GREETING");
            payload.put("staffId", staffId);
            payload.put("staffName", name);
            payload.put("text", "您好，我是客服" + name + "，很高兴为您服务。");
            session.sendMessage(new TextMessage(MAPPER.writeValueAsString(payload)));
        } catch (Exception ignored) {
        }
    }

    private void notifyStaffConnected(Long userId, Long staffId, WebSocketSession userSession) {
        if (userSession == null || !userSession.isOpen()) {
            return;
        }
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", "STAFF_CONNECTED");
            payload.put("staffId", staffId);
            payload.put("staffName", getStaffName(staffId));
            payload.put("userId", userId);
            userSession.sendMessage(new TextMessage(MAPPER.writeValueAsString(payload)));
        } catch (Exception ignored) {
        }
    }

    private String extractContent(String payload) {
        if (payload == null) {
            return null;
        }
        try {
            Map<String, Object> map = MAPPER.readValue(payload, new TypeReference<Map<String, Object>>() {
            });
            Object text = map.get("text");
            if (text != null) {
                return text.toString();
            }
            Object message = map.get("message");
            if (message != null) {
                return message.toString();
            }
        } catch (Exception ignored) {
        }
        return payload;
    }

    private Long resolveUserIdForStaff(Long staffId) {
        Long target = staffToUser.get(staffId);
        if (target != null) {
            return target;
        }
        return userToStaff.entrySet().stream()
                .filter(e -> e.getValue().equals(staffId))
                .map(Map.Entry::getKey)
                .findFirst().orElse(null);
    }

    private String getStaffName(Long staffId) {
        try {
            Staff staff = staffService.getById(staffId);
            if (staff != null && staff.getRealName() != null) {
                return staff.getRealName();
            }
        } catch (Exception ignored) {
        }
        return "客服" + staffId;
    }
}

class QueryUtil {
    static Map<String, String> parse(String query) {
        Map<String, String> map = new ConcurrentHashMap<>();
        if (query == null || query.isBlank())
            return map;
        String[] pairs = query.split("&");
        for (String p : pairs) {
            String[] kv = p.split("=");
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }
}
