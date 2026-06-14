package com.finalwork.soulcapsule.controller;

import com.finalwork.soulcapsule.common.ApiResult;
import com.finalwork.soulcapsule.dto.MoodRequest;
import com.finalwork.soulcapsule.entity.MoodRecord;
import com.finalwork.soulcapsule.service.MoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ApiResult<MoodRecord> add(@RequestBody MoodRequest request) {
        MoodRecord saved = moodService.addMoodFromRequest(request);
        return ApiResult.success("保存成功", saved);
    }

    /**
     * 更新心情记录
     */
    @PostMapping("/update")
    public ApiResult<MoodRecord> update(@RequestBody MoodRequest request) {
        MoodRecord updated = moodService.updateFromRequest(request);
        return ApiResult.success("更新成功", updated);
    }

    /**
     * 删除心情记录
     */
    @DeleteMapping("/delete/{id}")
    public ApiResult<Void> delete(@PathVariable("id") Long id) {
        moodService.deleteById(id);
        return ApiResult.success("删除成功", null);
    }

    /**
     * 查询指定用户的历史心情足迹（按 createTime 倒序，可选 keyword 模糊搜索 content）
     */
    @GetMapping("/list")
    public ApiResult<List<MoodRecord>> list(@RequestParam("userId") Long userId,
                                            @RequestParam(value = "keyword", required = false) String keyword) {
        List<MoodRecord> list = moodService.listMoods(userId, keyword);
        return ApiResult.success(list);
    }
}
