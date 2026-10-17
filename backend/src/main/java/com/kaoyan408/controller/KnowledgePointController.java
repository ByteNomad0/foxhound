package com.kaoyan408.controller;

import com.kaoyan408.common.Result;
import com.kaoyan408.entity.KnowledgePoint;
import com.kaoyan408.security.SecurityUtil;
import com.kaoyan408.service.KnowledgePointService;
import com.kaoyan408.service.StudyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge-points")
@RequiredArgsConstructor
public class KnowledgePointController {

    private final KnowledgePointService knowledgePointService;
    private final StudyService studyService;

    @GetMapping
    public Result<List<KnowledgePoint>> list(@RequestParam Long courseId) {
        return Result.ok(knowledgePointService.listByCourse(courseId));
    }

    @GetMapping("/{id}")
    public Result<KnowledgePoint> detail(@PathVariable Long id) {
        return Result.ok(knowledgePointService.getDetail(id));
    }

    /** 标记知识点为已学习（需登录） */
    @PostMapping("/{id}/studied")
    public Result<Void> markStudied(@PathVariable Long id) {
        studyService.markStudied(SecurityUtil.getUserId(), id);
        return Result.ok();
    }
}
