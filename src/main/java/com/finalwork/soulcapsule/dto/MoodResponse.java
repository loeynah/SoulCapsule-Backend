package com.finalwork.soulcapsule.dto;

import com.finalwork.soulcapsule.entity.MoodRecord;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 心情记录 API 响应 DTO（与前端 MoodResponse 字段对齐）
 */
@Data
public class MoodResponse {

    private Long id;
    private Integer score;
    private String emotions;
    private String content;
    private String imageUrl;
    private String tags;
    private String aiFeedback;
    private LocalDateTime createTime;

    public static MoodResponse fromEntity(MoodRecord record) {
        if (record == null) {
            return null;
        }
        MoodResponse response = new MoodResponse();
        response.setId(record.getId());
        response.setScore(record.getScore());
        response.setEmotions(record.getEmotions());
        response.setContent(record.getContent());
        response.setImageUrl(record.getImageUrl());
        response.setTags(record.getTags());
        response.setAiFeedback(record.getAiFeedback());
        response.setCreateTime(record.getCreateTime());
        return response;
    }
}
