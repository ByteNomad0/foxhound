package com.kaoyan408.controller;

import com.kaoyan408.common.Result;
import com.kaoyan408.dto.AiChatDTO;
import com.kaoyan408.security.SecurityUtil;
import com.kaoyan408.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /** AI 是否已配置并启用（无需登录） */
    @GetMapping("/config-status")
    public Result<Boolean> configStatus() {
        return Result.ok(aiService.configStatus());
    }

    /** 知识点答疑（SSE 流式输出） */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@Valid @RequestBody AiChatDTO dto) {
        return aiService.chatStream(SecurityUtil.getUserId(), dto);
    }

    /** 薄弱点分析（SSE 流式输出） */
    @PostMapping(value = "/weak-point-analysis", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter weakPointAnalysis() {
        return aiService.weakPointAnalysisStream(SecurityUtil.getUserId());
    }
}
