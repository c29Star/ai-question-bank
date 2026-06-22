package com.aiqb.ai;

import com.aiqb.entity.AiLog;
import com.aiqb.mapper.AiLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通义千问（DashScope）HTTP 调用服务
 * 兼容 OpenAI 格式，调用 /api/v1/services/aigc/text-generation/generation
 *
 * 注意：本类不依赖 dashscope-sdk-java，直接走 HTTP，更稳。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashScopeService {

    @Value("${ai.dashscope.api-key:}")
    private String apiKey;

    @Value("${ai.dashscope.base-url:https://dashscope.aliyuncs.com/api/v1}")
    private String baseUrl;

    @Value("${ai.dashscope.model:qwen-plus}")
    private String model;

    private final AiLogMapper aiLogMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public String chat(String systemPrompt, String userPrompt) {
        return chat(systemPrompt, userPrompt, null, null, null);
    }

    public String chat(String systemPrompt, String userPrompt, Long userId, String feature, String input) {
        long start = System.currentTimeMillis();
        AiLog logEntity = new AiLog();
        logEntity.setUserId(userId);
        logEntity.setFeature(feature);
        logEntity.setInput(truncate(input != null ? input : userPrompt, 4000));

        // 没配 key 就走模拟
        if (apiKey == null || apiKey.isEmpty() || "replace-with-your-key".equals(apiKey)) {
            logEntity.setStatus("FAILED");
            logEntity.setErrorMsg("未配置 DASHSCOPE_API_KEY");
            logEntity.setOutput("[模拟] 请在 application.yml 配置 DASHSCOPE_API_KEY");
            logEntity.setCostMs((int) (System.currentTimeMillis() - start));
            aiLogMapper.insert(logEntity);
            return "[模拟回答] 系统未配置 DashScope API Key，请在 application.yml 中配置 DASHSCOPE_API_KEY 后重启。\n\n你问的是：" + userPrompt;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", model);
            payload.put("input", buildInput(systemPrompt, userPrompt));
            payload.put("parameters", Map.of("temperature", 0.3, "top_p", 0.8, "result_format", "message"));

            String url = baseUrl + "/services/aigc/text-generation/generation";
            String body = objectMapper.writeValueAsString(payload);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            String respBody = resp.body();

            if (code != 200) {
                logEntity.setStatus("FAILED");
                logEntity.setErrorMsg("HTTP " + code + ": " + truncate(respBody, 500));
                logEntity.setCostMs((int) (System.currentTimeMillis() - start));
                aiLogMapper.insert(logEntity);
                return "[错误] DashScope 返回 " + code + ": " + respBody;
            }

            // 解析：output.choices[0].message.content
            Map<?, ?> root = objectMapper.readValue(respBody, Map.class);
            Map<?, ?> output = (Map<?, ?>) root.get("output");
            List<?> choices = (List<?>) output.get("choices");
            Map<?, ?> first = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) first.get("message");
            String text = (String) message.get("content");

            Map<?, ?> usage = (Map<?, ?>) root.get("usage");
            if (usage != null && usage.get("total_tokens") != null) {
                logEntity.setTokensUsed(((Number) usage.get("total_tokens")).intValue());
            }
            logEntity.setOutput(truncate(text, 4000));
            logEntity.setStatus("SUCCESS");
            logEntity.setCostMs((int) (System.currentTimeMillis() - start));
            aiLogMapper.insert(logEntity);
            return text;

        } catch (Exception e) {
            log.error("AI 调用失败", e);
            logEntity.setStatus("FAILED");
            logEntity.setErrorMsg(e.getMessage());
            logEntity.setCostMs((int) (System.currentTimeMillis() - start));
            aiLogMapper.insert(logEntity);
            return "[错误] AI 调用失败: " + e.getMessage();
        }
    }

    private Map<String, Object> buildInput(String systemPrompt, String userPrompt) {
        Map<String, Object> input = new HashMap<>();
        List<Map<String, String>> messages = new java.util.ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }
        messages.add(Map.of("role", "user", "content", userPrompt));
        input.put("messages", messages);
        return input;
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }
}
