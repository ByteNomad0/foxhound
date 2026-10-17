package com.kaoyan408.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kaoyan408.common.BizException;
import com.kaoyan408.common.JsonUtil;
import com.kaoyan408.common.PageResult;
import com.kaoyan408.dto.QuestionDTO;
import com.kaoyan408.entity.Course;
import com.kaoyan408.entity.KnowledgePoint;
import com.kaoyan408.entity.Question;
import com.kaoyan408.mapper.CourseMapper;
import com.kaoyan408.mapper.KnowledgePointMapper;
import com.kaoyan408.mapper.QuestionMapper;
import com.kaoyan408.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionMapper questionMapper;
    private final CourseMapper courseMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final JsonUtil jsonUtil;

    /** 练习模式：返回题目但不含答案与解析 */
    public List<QuestionVO> practice(Long courseId, Long knowledgePointId, String type, Integer difficulty, Integer count) {
        LambdaQueryWrapper<Question> w = new LambdaQueryWrapper<>();
        w.eq(Question::getStatus, 1);
        if (courseId != null) {
            w.eq(Question::getCourseId, courseId);
        }
        if (knowledgePointId != null) {
            w.eq(Question::getKnowledgePointId, knowledgePointId);
        }
        if (StringUtils.hasText(type)) {
            w.eq(Question::getType, type);
        }
        if (difficulty != null) {
            w.eq(Question::getDifficulty, difficulty);
        }
        w.orderByAsc(Question::getId);
        List<Question> list = questionMapper.selectList(w);
        if (count != null && count > 0 && list.size() > count) {
            Collections.shuffle(list);
            list = list.subList(0, count);
        }
        return list.stream().map(q -> toVO(q, false)).toList();
    }

    public QuestionVO getPracticeQuestion(Long id) {
        return toVO(requireQuestion(id), false);
    }

    /** 后台/复习：含答案与解析 */
    public QuestionVO getDetail(Long id) {
        return toVO(requireQuestion(id), true);
    }

    public PageResult<QuestionVO> adminList(int page, int size, Long courseId, String type, String keyword) {
        LambdaQueryWrapper<Question> w = new LambdaQueryWrapper<>();
        if (courseId != null) {
            w.eq(Question::getCourseId, courseId);
        }
        if (StringUtils.hasText(type)) {
            w.eq(Question::getType, type);
        }
        if (StringUtils.hasText(keyword)) {
            w.like(Question::getContent, keyword);
        }
        w.orderByDesc(Question::getId);
        Page<Question> p = questionMapper.selectPage(new Page<>(page, size), w);
        List<QuestionVO> vos = p.getRecords().stream().map(q -> toVO(q, true)).toList();
        return PageResult.of(vos, p.getTotal(), page, size);
    }

    public Question create(QuestionDTO dto) {
        Question q = new Question();
        apply(q, dto);
        questionMapper.insert(q);
        return q;
    }

    public Question update(Long id, QuestionDTO dto) {
        Question q = requireQuestion(id);
        apply(q, dto);
        questionMapper.updateById(q);
        return q;
    }

    public void delete(Long id) {
        requireQuestion(id);
        questionMapper.deleteById(id);
    }

    private Question requireQuestion(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null) {
            throw new BizException(404, "题目不存在");
        }
        return q;
    }

    private void apply(Question q, QuestionDTO dto) {
        q.setCourseId(dto.getCourseId());
        q.setKnowledgePointId(dto.getKnowledgePointId());
        q.setType(dto.getType());
        q.setContent(dto.getContent());
        q.setOptions(jsonUtil.toJson(dto.getOptions()));
        q.setAnswer(jsonUtil.toJson(dto.getAnswer()));
        q.setAnalysis(dto.getAnalysis());
        q.setDifficulty(dto.getDifficulty() == null ? 1 : dto.getDifficulty());
        q.setStatus(1);
    }

    private QuestionVO toVO(Question q, boolean includeAnswer) {
        QuestionVO vo = new QuestionVO();
        vo.setId(q.getId());
        vo.setCourseId(q.getCourseId());
        vo.setCourseName(courseName(q.getCourseId()));
        vo.setKnowledgePointId(q.getKnowledgePointId());
        vo.setKnowledgePointTitle(kpTitle(q.getKnowledgePointId()));
        vo.setType(q.getType());
        vo.setContent(q.getContent());
        vo.setOptions(jsonUtil.parseOptions(q.getOptions()));
        vo.setDifficulty(q.getDifficulty());
        if (includeAnswer) {
            vo.setAnswer(jsonUtil.parseStringList(q.getAnswer()));
            vo.setAnalysis(q.getAnalysis());
        }
        return vo;
    }

    private String courseName(Long id) {
        if (id == null) {
            return null;
        }
        Course c = courseMapper.selectById(id);
        return c == null ? null : c.getName();
    }

    private String kpTitle(Long id) {
        if (id == null) {
            return null;
        }
        KnowledgePoint k = knowledgePointMapper.selectById(id);
        return k == null ? null : k.getTitle();
    }
}
