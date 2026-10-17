package com.kaoyan408.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kaoyan408.common.BizException;
import com.kaoyan408.common.JsonUtil;
import com.kaoyan408.common.PageResult;
import com.kaoyan408.dto.AnswerSubmitDTO;
import com.kaoyan408.entity.AnswerRecord;
import com.kaoyan408.entity.Course;
import com.kaoyan408.entity.KnowledgePoint;
import com.kaoyan408.entity.Question;
import com.kaoyan408.mapper.AnswerRecordMapper;
import com.kaoyan408.mapper.CourseMapper;
import com.kaoyan408.mapper.KnowledgePointMapper;
import com.kaoyan408.mapper.QuestionMapper;
import com.kaoyan408.vo.AnswerRecordVO;
import com.kaoyan408.vo.AnswerResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRecordMapper answerRecordMapper;
    private final QuestionMapper questionMapper;
    private final CourseMapper courseMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final StudyService studyService;
    private final JsonUtil jsonUtil;

    /** 提交答案并自动判分 */
    public AnswerResultVO submit(Long userId, AnswerSubmitDTO dto) {
        Question q = questionMapper.selectById(dto.getQuestionId());
        if (q == null || q.getStatus() == null || q.getStatus() != 1) {
            throw new BizException(404, "题目不存在");
        }
        List<String> correctAnswer = jsonUtil.parseStringList(q.getAnswer());
        List<String> userAnswer = dto.getAnswer() == null ? new ArrayList<>() : dto.getAnswer();
        boolean correct = isCorrect(correctAnswer, userAnswer);

        AnswerRecord r = new AnswerRecord();
        r.setUserId(userId);
        r.setQuestionId(q.getId());
        r.setUserAnswer(jsonUtil.toJson(userAnswer));
        r.setIsCorrect(correct ? 1 : 0);
        r.setSubmittedAt(LocalDateTime.now());
        answerRecordMapper.insert(r);

        // 自动标记关联知识点为已学习
        if (q.getKnowledgePointId() != null) {
            studyService.markStudied(userId, q.getKnowledgePointId());
        }

        AnswerResultVO vo = new AnswerResultVO();
        vo.setQuestionId(q.getId());
        vo.setType(q.getType());
        vo.setCorrect(correct);
        vo.setUserAnswer(userAnswer);
        vo.setCorrectAnswer(correctAnswer);
        vo.setAnalysis(q.getAnalysis());
        return vo;
    }

    public PageResult<AnswerRecordVO> history(Long userId, int page, int size, Long courseId, Integer correct) {
        LambdaQueryWrapper<AnswerRecord> w = new LambdaQueryWrapper<>();
        w.eq(AnswerRecord::getUserId, userId);
        if (correct != null) {
            w.eq(AnswerRecord::getIsCorrect, correct);
        }
        w.orderByDesc(AnswerRecord::getId);
        Page<AnswerRecord> p = answerRecordMapper.selectPage(new Page<>(page, size), w);

        List<AnswerRecordVO> vos = new ArrayList<>();
        for (AnswerRecord r : p.getRecords()) {
            Question q = questionMapper.selectById(r.getQuestionId());
            if (q == null) {
                continue;
            }
            if (courseId != null && !courseId.equals(q.getCourseId())) {
                continue;
            }
            vos.add(toVO(r, q));
        }
        return PageResult.of(vos, p.getTotal(), page, size);
    }

    private boolean isCorrect(List<String> correct, List<String> user) {
        if (correct == null || user == null) {
            return false;
        }
        return new TreeSet<>(correct).equals(new TreeSet<>(user));
    }

    private AnswerRecordVO toVO(AnswerRecord r, Question q) {
        AnswerRecordVO vo = new AnswerRecordVO();
        vo.setId(r.getId());
        vo.setQuestionId(q.getId());
        vo.setQuestionContent(q.getContent());
        vo.setQuestionType(q.getType());
        vo.setOptions(jsonUtil.parseOptions(q.getOptions()));
        vo.setUserAnswer(jsonUtil.parseStringList(r.getUserAnswer()));
        vo.setCorrectAnswer(jsonUtil.parseStringList(q.getAnswer()));
        vo.setAnalysis(q.getAnalysis());
        vo.setIsCorrect(r.getIsCorrect());
        vo.setSubmittedAt(r.getSubmittedAt());

        Course c = courseMapper.selectById(q.getCourseId());
        vo.setCourseName(c == null ? null : c.getName());
        if (q.getKnowledgePointId() != null) {
            KnowledgePoint k = knowledgePointMapper.selectById(q.getKnowledgePointId());
            vo.setKnowledgePointTitle(k == null ? null : k.getTitle());
        }
        return vo;
    }
}
