package com.finalwork.soulcapsule.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finalwork.soulcapsule.config.ZhipuProperties;
import com.finalwork.soulcapsule.dto.MoodRequest;
import com.finalwork.soulcapsule.entity.MoodRecord;
import com.finalwork.soulcapsule.mapper.MoodRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MoodService {

    private static final String DEFAULT_MOOD_FEEDBACK = "我在这里，慢慢陪你。";

    private final MoodRecordMapper moodRecordMapper;
    private final AiService aiService;

    public MoodService(MoodRecordMapper moodRecordMapper, @Lazy AiService aiService) {
        this.moodRecordMapper = moodRecordMapper;
        this.aiService = aiService;
    }

    /**
     * 新增心情记录
     */
    public MoodRecord addMood(MoodRecord record) {
        if (record.getCreateTime() == null) {
            record.setCreateTime(LocalDateTime.now());
        }
        moodRecordMapper.insert(record);
        return record;
    }

    /**
     * 根据前端 MoodRequest 新增心情记录
     */
    public MoodRecord addMoodFromRequest(MoodRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }

        MoodRecord record = new MoodRecord();
        record.setUserId(request.getUserId());
        record.setScore(mapMoodTypeToScore(request.getMoodType()));
        record.setEmotions(request.getMoodType());
        record.setContent(request.getContent());
        record.setTags(request.getTags());
        record.setImageUrl(request.getImageUrl());
        record.setCreateTime(LocalDateTime.now());

        String aiFeedback = aiService.generateMoodFeedback(request.getContent());
        record.setAiFeedback(StringUtils.hasText(aiFeedback) ? aiFeedback : DEFAULT_MOOD_FEEDBACK);

        moodRecordMapper.insert(record);
        return record;
    }

    /**
     * 根据 ID 更新心情记录
     */
    public MoodRecord updateFromRequest(MoodRequest request) {
        if (request.getId() == null) {
            throw new IllegalArgumentException("id 不能为空");
        }

        MoodRecord existing = moodRecordMapper.selectById(request.getId());
        if (existing == null) {
            throw new IllegalArgumentException("心情记录不存在");
        }

        if (StringUtils.hasText(request.getMoodType())) {
            existing.setScore(mapMoodTypeToScore(request.getMoodType()));
            existing.setEmotions(request.getMoodType());
        }
        if (request.getContent() != null) {
            existing.setContent(request.getContent());
        }
        if (request.getTags() != null) {
            existing.setTags(request.getTags());
        }
        if (request.getImageUrl() != null) {
            existing.setImageUrl(request.getImageUrl());
        }

        moodRecordMapper.updateById(existing);
        return existing;
    }

    /**
     * 根据主键删除心情记录
     */
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id 不能为空");
        }
        MoodRecord existing = moodRecordMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("心情记录不存在");
        }
        moodRecordMapper.deleteById(id);
    }

    private Integer mapMoodTypeToScore(String moodType) {
        if (moodType == null) {
            return null;
        }
        return switch (moodType) {
            case "很好" -> 5;
            case "好" -> 4;
            case "一般" -> 3;
            case "不好" -> 2;
            case "很不好" -> 1;
            default -> 3;
        };
    }

    /**
     * 查询指定用户的历史心情足迹，按 createTime 倒序，支持 content 模糊搜索
     */
    public List<MoodRecord> listMoods(Long userId, String keyword) {
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        LambdaQueryWrapper<MoodRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MoodRecord::getUserId, userId);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(MoodRecord::getContent, keyword.trim());
        }
        wrapper.orderByDesc(MoodRecord::getCreateTime);
        return moodRecordMapper.selectList(wrapper);
    }

    /**
     * 查询用户最近 N 条心情记录，按 createTime 倒序
     */
    public List<MoodRecord> getRecentMoods(Long userId, int limit) {
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        LambdaQueryWrapper<MoodRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MoodRecord::getUserId, userId);
        wrapper.orderByDesc(MoodRecord::getCreateTime);
        wrapper.last("LIMIT " + limit);
        return moodRecordMapper.selectList(wrapper);
    }
}
