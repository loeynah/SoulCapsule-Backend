package com.finalwork.soulcapsule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatResponse {

    /** AI 回复文本 */
    private String reply;
}
