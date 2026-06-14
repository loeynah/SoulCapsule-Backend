package com.finalwork.soulcapsule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finalwork.soulcapsule.entity.MoodRecord;
import com.finalwork.soulcapsule.mapper.MoodRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MoodService {

    private final MoodRecordMapper moodRecordMapper;

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
     * 查询历史心情足迹，按 createTime 倒序
     */
    public List<MoodRecord> listMoods() {
        LambdaQueryWrapper<MoodRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(MoodRecord::getCreateTime);
        return moodRecordMapper.selectList(wrapper);
    }
}
