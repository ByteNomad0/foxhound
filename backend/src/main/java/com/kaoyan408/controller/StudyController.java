package com.kaoyan408.controller;

import com.kaoyan408.common.Result;
import com.kaoyan408.dto.DailyAnswerCount;
import com.kaoyan408.security.SecurityUtil;
import com.kaoyan408.service.StudyService;
import com.kaoyan408.vo.StatsVO;
import com.kaoyan408.vo.WeakPointVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    @GetMapping("/stats")
    public Result<StatsVO> stats() {
        return Result.ok(studyService.stats(SecurityUtil.getUserId()));
    }

    @GetMapping("/weak-points")
    public Result<List<WeakPointVO>> weakPoints() {
        return Result.ok(studyService.weakPoints(SecurityUtil.getUserId()));
    }

    @GetMapping("/studied-ids")
    public Result<List<Long>> studiedIds() {
        return Result.ok(studyService.getStudiedIds(SecurityUtil.getUserId()));
    }

    @GetMapping("/calendar")
    public Result<List<DailyAnswerCount>> calendar(@RequestParam int year, @RequestParam int month) {
        return Result.ok(studyService.calendar(SecurityUtil.getUserId(), year, month));
    }
}
