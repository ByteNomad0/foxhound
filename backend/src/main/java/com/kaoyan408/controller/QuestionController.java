package com.kaoyan408.controller;

import com.kaoyan408.common.Result;
import com.kaoyan408.service.QuestionService;
import com.kaoyan408.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /** 练习模式：获取题目（不含答案/解析） */
    @GetMapping("/practice")
    public Result<List<QuestionVO>> practice(@RequestParam(required = false) Long courseId,
                                             @RequestParam(required = false) Long knowledgePointId,
                                             @RequestParam(required = false) String type,
                                             @RequestParam(required = false) Integer difficulty,
                                             @RequestParam(required = false) Integer count) {
        return Result.ok(questionService.practice(courseId, knowledgePointId, type, difficulty, count));
    }

    /** 每日一题（公开，不含答案） */
    @GetMapping("/daily")
    public Result<QuestionVO> daily() {
        return Result.ok(questionService.daily());
    }

    @GetMapping("/{id}")
    public Result<QuestionVO> practiceDetail(@PathVariable Long id) {
        return Result.ok(questionService.getPracticeQuestion(id));
    }
}
