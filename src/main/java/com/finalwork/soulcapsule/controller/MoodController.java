package com.finalwork.soulcapsule.controller;

import com.finalwork.soulcapsule.common.ApiResult;
import com.finalwork.soulcapsule.entity.MoodRecord;
import com.finalwork.soulcapsule.service.MoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mood")
@RequiredArgsConstructor
public class MoodController {

    private final MoodService moodService;

    /**
     * 新增心情记录
     */
    @PostMapping("/add")
    public ApiResult<MoodRecord> add(@RequestBody MoodRecord record) {
        MoodRecord saved = moodService.addMood(record);
        return ApiResult.success("保存成功", saved);
    }

    /**
     * 查询历史心情足迹（按 createTime 倒序）
     */
    @GetMapping("/list")
    public ApiResult<List<MoodRecord>> list() {
        List<MoodRecord> list = moodService.listMoods();
        return ApiResult.success(list);
    }
}
