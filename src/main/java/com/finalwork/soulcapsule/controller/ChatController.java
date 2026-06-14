package com.finalwork.soulcapsule.controller;

import com.finalwork.soulcapsule.common.ApiResult;
import com.finalwork.soulcapsule.dto.ChatRequest;
import com.finalwork.soulcapsule.dto.ChatResponse;
import com.finalwork.soulcapsule.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 发送聊天消息，调用智谱 AI 并返回回复
     */
    @PostMapping("/send")
    public ApiResult<ChatResponse> send(@RequestBody ChatRequest request) {
        String reply = chatService.sendMessage(request);
        return ApiResult.success(new ChatResponse(reply));
    }
}
