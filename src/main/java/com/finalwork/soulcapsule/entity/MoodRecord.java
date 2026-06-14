package com.finalwork.soulcapsule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 心情记录实体，对应 mood_record 表
 */
@Data
@TableName("mood_record")
public class MoodRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;

    private Integer score;

    private String tags;

    private String emotions;

    private String content;

    private String aiFeedback;

    private LocalDateTime createTime;
}
