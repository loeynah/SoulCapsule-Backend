package com.finalwork.soulcapsule.dto;

import lombok.Data;

@Data
public class MoodRequest {

    /** 修改时使用的主键 ID */
    private Long id;

    /** 心情类型标识，如「很好」「一般」 */
    private String moodType;

    /** 用户日记内容 */
    private String content;

    /** 归因/情绪等标签，逗号分隔 */
    private String tags;

    /** 图片访问 URL */
    private String imageUrl;

    /** 当前登录用户 ID */
    private Long userId;
}
