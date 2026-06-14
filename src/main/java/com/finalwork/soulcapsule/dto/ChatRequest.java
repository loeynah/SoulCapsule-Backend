package com.finalwork.soulcapsule.dto;

import lombok.Data;

@Data
public class ChatRequest {

    /** 当前登录用户 ID */
    private Long userId;

    /** 用户发送的聊天文本 */
    private String message;
}
