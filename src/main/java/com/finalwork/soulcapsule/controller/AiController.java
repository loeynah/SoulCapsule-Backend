package com.finalwork.soulcapsule.controller;

import com.finalwork.soulcapsule.common.ApiResult;
import com.finalwork.soulcapsule.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * 基于用户近期心情与聊天记录，生成个性化解压小锦囊。
     */
    @GetMapping("/tips")
    public ApiResult<List<String>> getDecompressionTips(@RequestParam("userId") Long userId) {
        List<String> tips = aiService.getDecompressionTips(userId);
        return ApiResult.success(tips);
    }
}
