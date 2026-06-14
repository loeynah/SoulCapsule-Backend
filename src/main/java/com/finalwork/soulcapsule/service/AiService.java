package com.finalwork.soulcapsule.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.finalwork.soulcapsule.config.ZhipuProperties;
import com.finalwork.soulcapsule.entity.ChatMessage;
import com.finalwork.soulcapsule.entity.MoodRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private static final String ZHIPU_CHAT_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static final String MODEL = "glm-4-flash";
    private static final int MOOD_LIMIT = 5;
    private static final int CHAT_LIMIT = 10;

    private static final List<String> DEFAULT_TIPS = List.of(
            "去吹吹晚风吧",
            "喝一杯热牛奶"
    );

    private static final Pattern JSON_ARRAY_PATTERN = Pattern.compile("\\[.*]", Pattern.DOTALL);

    private final ZhipuProperties zhipuProperties;
    private final MoodService moodService;
    private final ChatMessageService chatMessageService;

    /**
     * 基于用户近期心情与聊天记录，生成 2~3 条个性化解压建议。
     */
    public List<String> getDecompressionTips(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }

        List<MoodRecord> recentMoods = moodService.getRecentMoods(userId, MOOD_LIMIT);
        List<ChatMessage> recentChats = chatMessageService.getRecentMessagesAsc(userId, CHAT_LIMIT);

        if (recentMoods.isEmpty() && recentChats.isEmpty()) {
            return copyDefaultTips();
        }

        String apiKey = zhipuProperties.getKey();
        if (!StringUtils.hasText(apiKey)) {
            log.warn("智谱 API Key 未配置，返回默认解压小锦囊");
            return copyDefaultTips();
        }

        try {
            String systemPrompt = buildTipsSystemPrompt(recentMoods, recentChats);
            String aiReply = callZhipuForTips(apiKey, systemPrompt);
            List<String> parsed = parseTipsFromAiResponse(aiReply);
            if (parsed.isEmpty()) {
                return copyDefaultTips();
            }
            return parsed;
        } catch (Exception e) {
            log.error("生成解压小锦囊失败，userId={}", userId, e);
            return copyDefaultTips();
        }
    }

    /**
     * 根据用户日记内容生成一句简短的治愈系 AI 回复。
     */
    public String generateMoodFeedback(String content) {
        if (!StringUtils.hasText(content)) {
            return "我在这里，慢慢陪你。";
        }

        String apiKey = zhipuProperties.getKey();
        if (!StringUtils.hasText(apiKey)) {
            log.warn("智谱 API Key 未配置，使用默认心情回复");
            return "我在这里，慢慢陪你。";
        }

        try {
            String systemPrompt = "用户写了一篇日记：[" + content.trim()
                    + "]，请作为树洞助手小旅，给出15字以内的温暖抱抱或鼓励。"
                    + "只输出一句话，不要引号，不要 Markdown。";
            String reply = callZhipuSimple(apiKey, systemPrompt, "请给出你的回复。");
            if (!StringUtils.hasText(reply)) {
                return "我在这里，慢慢陪你。";
            }
            return reply.trim();
        } catch (Exception e) {
            log.error("生成心情 AI 回复失败", e);
            return "我在这里，慢慢陪你。";
        }
    }

    private String callZhipuSimple(String apiKey, String systemPrompt, String userPrompt) {
        JSONArray messages = new JSONArray();

        JSONObject systemMessage = new JSONObject();
        systemMessage.set("role", "system");
        systemMessage.set("content", systemPrompt);
        messages.add(systemMessage);

        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", userPrompt);
        messages.add(userMessage);

        JSONObject body = new JSONObject();
        body.set("model", MODEL);
        body.set("messages", messages);

        try (HttpResponse response = HttpRequest.post(ZHIPU_CHAT_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .timeout(30_000)
                .execute()) {

            String responseBody = response.body();
            if (!response.isOk()) {
                throw new RuntimeException("智谱 API 请求失败，HTTP " + response.getStatus());
            }

            JSONObject json = JSONUtil.parseObj(responseBody);
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("智谱 API 返回为空");
            }
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            if (message == null) {
                throw new RuntimeException("智谱 API 响应格式异常");
            }
            return message.getStr("content", "");
        }
    }

    private String buildTipsSystemPrompt(List<MoodRecord> moods, List<ChatMessage> chats) {
        StringBuilder dataSection = new StringBuilder();

        dataSection.append("【近期心情记录】\n");
        if (moods.isEmpty()) {
            dataSection.append("（暂无心情记录）\n");
        } else {
            for (MoodRecord mood : moods) {
                dataSection.append("- 时间: ").append(mood.getCreateTime())
                        .append("，心情: ").append(nullToEmpty(mood.getEmotions()))
                        .append("，分数: ").append(mood.getScore())
                        .append("，内容: ").append(nullToEmpty(mood.getContent()))
                        .append('\n');
            }
        }

        dataSection.append("\n【近期聊天记录】\n");
        if (chats.isEmpty()) {
            dataSection.append("（暂无聊天记录）\n");
        } else {
            for (ChatMessage chat : chats) {
                dataSection.append("- [").append(nullToEmpty(chat.getRole())).append("] ")
                        .append(nullToEmpty(chat.getContent()))
                        .append('\n');
            }
        }

        return "你是一个心理疗愈师。以下是该用户近期的心情记录和跟你的聊天记录：\n"
                + dataSection
                + "\n请根据这些信息，为他量身定制 2 到 3 条简短、治愈、且具有实操性的解压建议。"
                + "请严格以 JSON 字符串数组的格式返回，例如 [\"建议1\", \"建议2\"]，"
                + "不要包含其他任何格式化字符或 Markdown 标记。";
    }

    private String callZhipuForTips(String apiKey, String systemPrompt) {
        JSONArray messages = new JSONArray();

        JSONObject systemMessage = new JSONObject();
        systemMessage.set("role", "system");
        systemMessage.set("content", systemPrompt);
        messages.add(systemMessage);

        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", "请根据以上信息，现在生成解压建议。");
        messages.add(userMessage);

        JSONObject body = new JSONObject();
        body.set("model", MODEL);
        body.set("messages", messages);

        try (HttpResponse response = HttpRequest.post(ZHIPU_CHAT_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .timeout(60_000)
                .execute()) {

            String responseBody = response.body();
            log.debug("智谱解压锦囊 API 响应: {}", responseBody);

            if (!response.isOk()) {
                throw new RuntimeException("智谱 API 请求失败，HTTP " + response.getStatus() + ": " + responseBody);
            }

            JSONObject json = JSONUtil.parseObj(responseBody);
            JSONArray choices = json.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("智谱 API 返回为空");
            }
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            if (message == null) {
                throw new RuntimeException("智谱 API 响应格式异常");
            }
            return message.getStr("content", "");
        }
    }

    private List<String> parseTipsFromAiResponse(String content) {
        if (!StringUtils.hasText(content)) {
            return Collections.emptyList();
        }

        String candidate = extractJsonArray(content.trim());
        if (!StringUtils.hasText(candidate)) {
            return Collections.emptyList();
        }

        try {
            JSONArray array = JSONUtil.parseArray(candidate);
            List<String> tips = new ArrayList<>();
            for (Object item : array) {
                if (item == null) {
                    continue;
                }
                String tip = item.toString().trim();
                if (StringUtils.hasText(tip)) {
                    tips.add(tip);
                }
            }
            return normalizeTips(tips);
        } catch (Exception e) {
            log.warn("解析 AI 解压锦囊 JSON 失败: {}", content, e);
            return Collections.emptyList();
        }
    }

    private String extractJsonArray(String content) {
        if (content.startsWith("```")) {
            content = content.replaceAll("^```(?:json)?\\s*", "")
                    .replaceAll("\\s*```$", "")
                    .trim();
        }
        if (content.startsWith("[") && content.endsWith("]")) {
            return content;
        }
        Matcher matcher = JSON_ARRAY_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group();
        }
        return content;
    }

    private List<String> normalizeTips(List<String> tips) {
        if (tips.isEmpty()) {
            return Collections.emptyList();
        }
        int size = Math.min(3, tips.size());
        List<String> result = new ArrayList<>(tips.subList(0, size));
        while (result.size() < 2) {
            result.add(DEFAULT_TIPS.get(result.size() % DEFAULT_TIPS.size()));
        }
        return result;
    }

    private List<String> copyDefaultTips() {
        return new ArrayList<>(DEFAULT_TIPS);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
