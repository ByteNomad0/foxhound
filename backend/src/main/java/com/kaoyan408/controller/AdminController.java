package com.kaoyan408.controller;

import com.kaoyan408.common.PageResult;
import com.kaoyan408.common.Result;
import com.kaoyan408.dto.AiConfigDTO;
import com.kaoyan408.dto.CodingProblemDTO;
import com.kaoyan408.dto.CodingTestCaseDTO;
import com.kaoyan408.dto.CourseDTO;
import com.kaoyan408.dto.KnowledgePointDTO;
import com.kaoyan408.dto.QuestionDTO;
import com.kaoyan408.entity.AiConfig;
import com.kaoyan408.entity.CodingProblem;
import com.kaoyan408.entity.CodingTestCase;
import com.kaoyan408.entity.Course;
import com.kaoyan408.entity.KnowledgePoint;
import com.kaoyan408.entity.Question;
import com.kaoyan408.entity.User;
import com.kaoyan408.service.AdminService;
import com.kaoyan408.service.AiService;
import com.kaoyan408.service.CodingProblemService;
import com.kaoyan408.service.CourseService;
import com.kaoyan408.service.KnowledgePointService;
import com.kaoyan408.service.QuestionService;
import com.kaoyan408.vo.DashboardVO;
import com.kaoyan408.vo.QuestionVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final CourseService courseService;
    private final KnowledgePointService knowledgePointService;
    private final QuestionService questionService;
    private final AiService aiService;
    private final CodingProblemService codingProblemService;

    // ---------- 数据看板 ----------
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.ok(adminService.dashboard());
    }

    // ---------- 用户管理 ----------
    @GetMapping("/users")
    public Result<PageResult<User>> users(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size,
                                          @RequestParam(required = false) String keyword) {
        return Result.ok(adminService.listUsers(page, size, keyword));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminService.updateStatus(id, status);
        return Result.ok();
    }

    @PutMapping("/users/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @RequestParam String role) {
        adminService.updateRole(id, role);
        return Result.ok();
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return Result.ok();
    }

    // ---------- 课程管理 ----------
    @GetMapping("/courses")
    public Result<List<Course>> courses() {
        return Result.ok(courseService.listAll());
    }

    @PostMapping("/courses")
    public Result<Course> createCourse(@Valid @RequestBody CourseDTO dto) {
        return Result.ok(courseService.create(dto));
    }

    @PutMapping("/courses/{id}")
    public Result<Course> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDTO dto) {
        return Result.ok(courseService.update(id, dto));
    }

    @DeleteMapping("/courses/{id}")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        courseService.delete(id);
        return Result.ok();
    }

    // ---------- 知识点管理 ----------
    @GetMapping("/knowledge-points")
    public Result<List<KnowledgePoint>> knowledgePoints(@RequestParam Long courseId) {
        return Result.ok(knowledgePointService.listByCourse(courseId));
    }

    @PostMapping("/knowledge-points")
    public Result<KnowledgePoint> createKp(@Valid @RequestBody KnowledgePointDTO dto) {
        return Result.ok(knowledgePointService.create(dto));
    }

    @PutMapping("/knowledge-points/{id}")
    public Result<KnowledgePoint> updateKp(@PathVariable Long id, @Valid @RequestBody KnowledgePointDTO dto) {
        return Result.ok(knowledgePointService.update(id, dto));
    }

    @DeleteMapping("/knowledge-points/{id}")
    public Result<Void> deleteKp(@PathVariable Long id) {
        knowledgePointService.delete(id);
        return Result.ok();
    }

    // ---------- 题库管理 ----------
    @GetMapping("/questions")
    public Result<PageResult<QuestionVO>> questions(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @RequestParam(required = false) Long courseId,
                                                    @RequestParam(required = false) String type,
                                                    @RequestParam(required = false) String keyword) {
        return Result.ok(questionService.adminList(page, size, courseId, type, keyword));
    }

    @GetMapping("/questions/{id}")
    public Result<QuestionVO> questionDetail(@PathVariable Long id) {
        return Result.ok(questionService.getDetail(id));
    }

    @PostMapping("/questions")
    public Result<Question> createQuestion(@Valid @RequestBody QuestionDTO dto) {
        return Result.ok(questionService.create(dto));
    }

    @PutMapping("/questions/{id}")
    public Result<Question> updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionDTO dto) {
        return Result.ok(questionService.update(id, dto));
    }

    @DeleteMapping("/questions/{id}")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        questionService.delete(id);
        return Result.ok();
    }

    // ---------- 算法题管理 ----------
    @GetMapping("/coding/problems")
    public Result<List<CodingProblem>> codingProblems() {
        return Result.ok(codingProblemService.listAll());
    }

    @PostMapping("/coding/problems")
    public Result<CodingProblem> createCodingProblem(@Valid @RequestBody CodingProblemDTO dto) {
        return Result.ok(codingProblemService.create(dto));
    }

    @PutMapping("/coding/problems/{id}")
    public Result<CodingProblem> updateCodingProblem(@PathVariable Long id, @Valid @RequestBody CodingProblemDTO dto) {
        return Result.ok(codingProblemService.update(id, dto));
    }

    @DeleteMapping("/coding/problems/{id}")
    public Result<Void> deleteCodingProblem(@PathVariable Long id) {
        codingProblemService.delete(id);
        return Result.ok();
    }

    @GetMapping("/coding/test-cases")
    public Result<List<CodingTestCase>> codingTestCases(@RequestParam Long problemId) {
        return Result.ok(codingProblemService.listTestCases(problemId));
    }

    @PostMapping("/coding/test-cases")
    public Result<CodingTestCase> createCodingTestCase(@Valid @RequestBody CodingTestCaseDTO dto) {
        return Result.ok(codingProblemService.createTestCase(dto));
    }

    @PutMapping("/coding/test-cases/{id}")
    public Result<CodingTestCase> updateCodingTestCase(@PathVariable Long id, @Valid @RequestBody CodingTestCaseDTO dto) {
        return Result.ok(codingProblemService.updateTestCase(id, dto));
    }

    @DeleteMapping("/coding/test-cases/{id}")
    public Result<Void> deleteCodingTestCase(@PathVariable Long id) {
        codingProblemService.deleteTestCase(id);
        return Result.ok();
    }

    // ---------- AI 配置 ----------
    @GetMapping("/ai-config")
    public Result<AiConfig> aiConfig() {
        return Result.ok(aiService.getConfig());
    }

    @PutMapping("/ai-config")
    public Result<Void> saveAiConfig(@RequestBody AiConfigDTO dto) {
        aiService.saveConfig(dto);
        return Result.ok();
    }
}
