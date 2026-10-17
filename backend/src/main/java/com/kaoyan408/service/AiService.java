package com.kaoyan408.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaoyan408.common.BizException;
import com.kaoyan408.dto.AiChatDTO;
import com.kaoyan408.dto.AiConfigDTO;
import com.kaoyan408.entity.AiChatRecord;
import com.kaoyan408.entity.AiConfig;
import com.kaoyan408.entity.Course;
import com.kaoyan408.entity.KnowledgePoint;
import com.kaoyan408.mapper.AiChatRecordMapper;
import com.kaoyan408.mapper.AiConfigMapper;
import com.kaoyan408.mapper.CourseMapper;
import com.kaoyan408.mapper.KnowledgePointMapper;
import com.kaoyan408.vo.WeakPointVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private static final String DEFAULT_PROMPT =
            "你是408计算机考研学习助手，精通数据结构、计算机组成原理、操作系统、计算机网络。" +
            "请使用中文、准确、简洁地解答问题，帮助考生理解知识点、掌握解题方法。";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private final AiConfigMapper aiConfigMapper;
    private final AiChatRecordMapper aiChatRecordMapper;
    private final StudyService studyService;
    private final CourseMapper courseMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final ObjectMapper objectMapper;

    @Autowired
    @Qualifier("aiExecutor")
    private ThreadPoolTaskExecutor aiExecutor;

    // ---------------- 配置 ----------------

    public AiConfig getConfig() {
        AiConfig c = getConfigEntity();
        if (c != null) {
            c.setApiKey(mask(c.getApiKey()));
        }
        return c;
    }

    public boolean configStatus() {
        return isConfigured();
    }

    public void saveConfig(AiConfigDTO dto) {
        AiConfig c = getConfigEntity();
        if (c == null) {
            c = new AiConfig();
        }
        c.setProvider(dto.getProvider() == null || dto.getProvider().isBlank() ? "openai" : dto.getProvider());
        c.setBaseUrl(dto.getBaseUrl());
        c.setModel(dto.getModel());
        c.setSystemPrompt(dto.getSystemPrompt());
        c.setEnabled(dto.getEnabled());
        String key = dto.getApiKey();
        if (key != null && !key.isBlank() && !key.startsWith("****")) {
            c.setApiKey(key);
        }
        if (c.getId() == null) {
            aiConfigMapper.insert(c);
        } else {
            aiConfigMapper.updateById(c);
        }
    }

    // ---------------- 流式对话 ----------------

    /** 知识点答疑（SSE 流式输出） */
    public SseEmitter chatStream(Long userId, AiChatDTO dto) {
        AiConfig c = requireConfig();
        String userMsg = buildUserMessage(dto);
        List<Map<String, String>> messages = buildMessages(c, userMsg);
        return stream(userId, dto.getMessage(), c, messages);
    }

    /** 薄弱点分析（SSE 流式输出） */
    public SseEmitter weakPointAnalysisStream(Long userId) {
        AiConfig c = requireConfig();
        List<WeakPointVO> weak = studyService.weakPoints(userId).stream()
                .filter(w -> w.getTotal() != null && w.getTotal() >= 2 && w.getAccuracy() != null && w.getAccuracy() < 60)
                .toList();
        if (weak.isEmpty()) {
            String reply = "根据你目前的答题记录，尚未发现明显薄弱点（答题量较少或正确率良好）。" +
                    "建议继续刷题积累数据，AI 将为你提供更精准的分析。";
            return streamFixed(userId, "[薄弱点分析请求]", reply);
        }
        StringBuilder sb = new StringBuilder("以下是我的薄弱知识点（正确率低于60%）：\n");
        for (WeakPointVO w : weak) {
            sb.append("- ").append(w.getCourseName()).append(" / ").append(w.getKnowledgePointTitle())
                    .append("：共答 ").append(w.getTotal()).append(" 题，正确 ").append(w.getCorrect())
                    .append(" 题，正确率 ").append(w.getAccuracy()).append("%\n");
        }
        sb.append("\n请分析我的薄弱环节，并给出针对性的复习建议和重点突破方向。");
        List<Map<String, String>> messages = buildMessages(c, sb.toString());
        return stream(userId, "[薄弱点分析请求]", c, messages);
    }

    // ---------------- 内部实现 ----------------

    /** 通过线程池异步调用大模型流式接口，并把增量转发给前端 */
    private SseEmitter stream(Long userId, String userRecord, AiConfig c, List<Map<String, String>> messages) {
        SseEmitter emitter = new SseEmitter(300_000L);
        aiExecutor.execute(() -> {
            StringBuilder full = new StringBuilder();
            try {
                streamChat(c, messages, chunk -> {
                    full.append(chunk);
                    try {
                        emitter.send(SseEmitter.event().data(jsonString(chunk)));
                    } catch (IOException e) {
                        throw new RuntimeException("client disconnected", e);
                    }
                });
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                log.error("AI 流式调用异常", e);
                try {
                    emitter.send(SseEmitter.event().data("[ERROR]"));
                    emitter.send(SseEmitter.event().data(jsonString(e.getMessage())));
                } catch (IOException ignored) {
                }
                emitter.complete();
            } finally {
                if (userRecord != null) {
                    safeSaveChat(userId, "user", userRecord);
                }
                if (full.length() > 0) {
                    safeSaveChat(userId, "assistant", full.toString());
                }
            }
        });
        return emitter;
    }

    /** 无需调用大模型时，直接以流式形式返回固定文本 */
    private SseEmitter streamFixed(Long userId, String userRecord, String reply) {
        SseEmitter emitter = new SseEmitter(60_000L);
        aiExecutor.execute(() -> {
            try {
                emitter.send(SseEmitter.event().data(jsonString(reply)));
                emitter.send(SseEmitter.event().data("[DONE]"));
                emitter.complete();
            } catch (IOException e) {
                emitter.complete();
            }
            safeSaveChat(userId, "user", userRecord);
            safeSaveChat(userId, "assistant", reply);
        });
        return emitter;
    }

    /** 调用 OpenAI 兼容的流式接口，逐 token 回调 */
    private void streamChat(AiConfig c, List<Map<String, String>> messages, Consumer<String> onChunk) {
        String base = c.getBaseUrl().endsWith("/") ? c.getBaseUrl() : c.getBaseUrl() + "/";
        String url = base + "chat/completions";
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", c.getModel());
            body.put("messages", messages);
            body.put("temperature", 0.7);
            body.put("stream", true);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + c.getApiKey())
                    .timeout(Duration.ofMinutes(5))
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
            int status = response.statusCode();
            if (status != 200) {
                String errBody = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
                throw new BizException(500, "AI 调用失败(" + status + ")：" + truncate(errBody));
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank() || !line.startsWith("data:")) {
                        continue;
                    }
                    String data = line.substring(5).trim();
                    if (data.isEmpty() || "[DONE]".equals(data)) {
                        continue;
                    }
                    try {
                        JsonNode node = objectMapper.readTree(data);
                        String content = node.path("choices").path(0).path("delta").path("content").asText(null);
                        if (content != null && !content.isEmpty()) {
                            onChunk.accept(content);
                        }
                    } catch (JsonProcessingException ignored) {
                        // 忽略无法解析的行
                    }
                }
            }
        } catch (BizException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(500, "AI 调用被中断");
        } catch (IOException e) {
            throw new BizException(500, "AI 调用失败：" + e.getMessage());
        }
    }

    private String buildUserMessage(AiChatDTO dto) {
        String userMsg = dto.getMessage();
        if (dto.getKnowledgePointId() != null) {
            KnowledgePoint kp = knowledgePointMapper.selectById(dto.getKnowledgePointId());
            if (kp != null) {
                userMsg = "【知识点：" + kp.getTitle() + "】\n" +
                        (kp.getContent() == null ? "" : kp.getContent()) +
                        "\n\n【用户问题】" + dto.getMessage();
            }
        } else if (dto.getCourseId() != null) {
            Course course = courseMapper.selectById(dto.getCourseId());
            if (course != null) {
                userMsg = "【课程：" + course.getName() + "】\n【用户问题】" + dto.getMessage();
            }
        }
        return userMsg;
    }

    private List<Map<String, String>> buildMessages(AiConfig c, String userMsg) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt(c)));
        messages.add(Map.of("role", "user", "content", userMsg));
        return messages;
    }

    private AiConfig getConfigEntity() {
        List<AiConfig> list = aiConfigMapper.selectList(null);
        return list.isEmpty() ? null : list.get(0);
    }

    private boolean isConfigured() {
        AiConfig c = getConfigEntity();
        return c != null && c.getEnabled() != null && c.getEnabled() == 1
                && notBlank(c.getApiKey()) && notBlank(c.getBaseUrl()) && notBlank(c.getModel());
    }

    private AiConfig requireConfig() {
        if (!isConfigured()) {
            throw new BizException(400, "AI 助手尚未配置或未启用，请联系管理员在后台配置 API 密钥");
        }
        return getConfigEntity();
    }

    private String systemPrompt(AiConfig c) {
        return notBlank(c.getSystemPrompt()) ? c.getSystemPrompt() : DEFAULT_PROMPT;
    }

    private String jsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new BizException("序列化失败");
        }
    }

    private void safeSaveChat(Long userId, String role, String content) {
        try {
            AiChatRecord r = new AiChatRecord();
            r.setUserId(userId);
            r.setRole(role);
            r.setContent(content);
            r.setCreatedAt(LocalDateTime.now());
            aiChatRecordMapper.insert(r);
        } catch (Exception e) {
            log.error("保存AI对话记录失败", e);
        }
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    private String mask(String key) {
        if (key == null || key.length() <= 4) {
            return key;
        }
        return "****" + key.substring(key.length() - 4);
    }

    private String truncate(String s) {
        if (s == null) {
            return "";
        }
        return s.length() > 300 ? s.substring(0, 300) : s;
    }
}
