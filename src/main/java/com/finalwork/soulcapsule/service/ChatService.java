package com.finalwork.soulcapsule.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.finalwork.soulcapsule.config.ZhipuProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String ZHIPU_CHAT_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    private static final String MODEL = "glm-4-flash";

    private static final String SYSTEM_PROMPT =
            "你叫小旅，是一个温暖、治愈、懂一点心理学的情绪树洞伴侣。你现在正在和一个信任你的大学生聊天。"
                    + "请绝对不要暴露你是人工智能、大模型或 AI 助手。说话要像真人朋友一样自然、温柔、有同理心，"
                    + "并且回复尽量简短（控制在 50 字以内），多用疑问句引导对方倾诉。";

    private final ZhipuProperties zhipuProperties;

    /**
     * 调用智谱大模型 API，返回 AI 回复文本
     */
    public String sendMessage(String userMessage) {
        if (!StringUtils.hasText(userMessage)) {
            throw new IllegalArgumentException("消息内容不能为空");
        }

        String apiKey = zhipuProperties.getKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("智谱 API Key 未配置，请在 application.yml 中设置 zhipu.api.key");
        }

        return callZhipuApi(apiKey, userMessage);
    }

    /**
     * 调用智谱 GLM-4-Flash 对话接口
     */
    private String callZhipuApi(String apiKey, String userMessage) {
        JSONObject requestBody = buildRequestBody(userMessage);

        try (HttpResponse response = HttpRequest.post(ZHIPU_CHAT_URL)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .timeout(60_000)
                .execute()) {

            String body = response.body();
            log.debug("智谱 API 响应: {}", body);

            if (!response.isOk()) {
                throw new RuntimeException("智谱 API 请求失败，HTTP " + response.getStatus() + ": " + body);
            }

            return parseReply(body);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("调用智谱 API 异常: " + e.getMessage(), e);
        }
    }

    private JSONObject buildRequestBody(String userMessage) {
        JSONArray messages = new JSONArray();

        JSONObject systemMessage = new JSONObject();
        systemMessage.set("role", "system");
        systemMessage.set("content", SYSTEM_PROMPT);
        messages.add(systemMessage);

        JSONObject userMsg = new JSONObject();
        userMsg.set("role", "user");
        userMsg.set("content", userMessage);
        messages.add(userMsg);

        JSONObject body = new JSONObject();
        body.set("model", MODEL);
        body.set("messages", messages);
        return body;
    }

    private String parseReply(String responseBody) {
        JSONObject json = JSONUtil.parseObj(responseBody);
        JSONArray choices = json.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("智谱 API 返回为空: " + responseBody);
        }
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject message = firstChoice.getJSONObject("message");
        if (message == null) {
            throw new RuntimeException("智谱 API 响应格式异常: " + responseBody);
        }
        return message.getStr("content", "");
    }
}
