package com.finalwork.soulcapsule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finalwork.soulcapsule.entity.ChatMessage;
import com.finalwork.soulcapsule.mapper.ChatMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private static final int CONTEXT_WINDOW_SIZE = 6;

    private final ChatMessageMapper chatMessageMapper;

    /**
     * 查询用户最近 N 条聊天记录，按时间正序（由远及近）返回，供 AI 上下文使用。
     */
    public List<ChatMessage> getRecentMessagesAsc(Long userId, int limit) {
        if (userId == null) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getUserId, userId);
        wrapper.orderByDesc(ChatMessage::getCreateTime);
        wrapper.last("LIMIT " + limit);

        List<ChatMessage> recentDesc = chatMessageMapper.selectList(wrapper);
        if (recentDesc.isEmpty()) {
            return recentDesc;
        }

        List<ChatMessage> recentAsc = new ArrayList<>(recentDesc);
        Collections.reverse(recentAsc);
        return recentAsc;
    }

    public List<ChatMessage> getRecentContextMessages(Long userId) {
        return getRecentMessagesAsc(userId, CONTEXT_WINDOW_SIZE);
    }

    public void saveMessage(Long userId, String role, String content) {
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        if (!StringUtils.hasText(role)) {
            throw new IllegalArgumentException("role 不能为空");
        }
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("content 不能为空");
        }

        ChatMessage message = new ChatMessage();
        message.setUserId(userId);
        message.setRole(role.trim());
        message.setContent(content.trim());
        message.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(message);
    }
}
