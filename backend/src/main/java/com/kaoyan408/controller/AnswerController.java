package com.kaoyan408.controller;

import com.kaoyan408.common.PageResult;
import com.kaoyan408.common.Result;
import com.kaoyan408.dto.AnswerSubmitDTO;
import com.kaoyan408.security.SecurityUtil;
import com.kaoyan408.service.AnswerService;
import com.kaoyan408.vo.AnswerRecordVO;
import com.kaoyan408.vo.AnswerResultVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping("/submit")
    public Result<AnswerResultVO> submit(@Valid @RequestBody AnswerSubmitDTO dto) {
        return Result.ok(answerService.submit(SecurityUtil.getUserId(), dto));
    }

    @GetMapping("/history")
    public Result<PageResult<AnswerRecordVO>> history(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) Long courseId,
                                                      @RequestParam(required = false) Integer correct) {
        return Result.ok(answerService.history(SecurityUtil.getUserId(), page, size, courseId, correct));
    }
}
