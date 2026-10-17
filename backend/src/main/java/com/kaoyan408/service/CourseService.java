package com.kaoyan408.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kaoyan408.common.BizException;
import com.kaoyan408.dto.CourseDTO;
import com.kaoyan408.entity.Course;
import com.kaoyan408.entity.KnowledgePoint;
import com.kaoyan408.entity.Question;
import com.kaoyan408.mapper.CourseMapper;
import com.kaoyan408.mapper.KnowledgePointMapper;
import com.kaoyan408.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseMapper courseMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final QuestionMapper questionMapper;

    /** 公开：启用中的课程，按 sort 排序 */
    public List<Course> listActive() {
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getStatus, 1)
                .orderByAsc(Course::getSort));
    }

    /** 后台：全部课程 */
    public List<Course> listAll() {
        return courseMapper.selectList(new LambdaQueryWrapper<Course>().orderByAsc(Course::getSort));
    }

    public Course getById(Long id) {
        Course c = courseMapper.selectById(id);
        if (c == null) {
            throw new BizException(404, "课程不存在");
        }
        return c;
    }

    public Course create(CourseDTO dto) {
        Course c = new Course();
        apply(c, dto);
        courseMapper.insert(c);
        return c;
    }

    public Course update(Long id, CourseDTO dto) {
        Course c = getById(id);
        apply(c, dto);
        courseMapper.updateById(c);
        return c;
    }

    public void delete(Long id) {
        getById(id);
        Long kpCount = knowledgePointMapper.selectCount(
                new LambdaQueryWrapper<KnowledgePoint>().eq(KnowledgePoint::getCourseId, id));
        Long qCount = questionMapper.selectCount(
                new LambdaQueryWrapper<Question>().eq(Question::getCourseId, id));
        if ((kpCount != null && kpCount > 0) || (qCount != null && qCount > 0)) {
            throw new BizException(400, "该课程下存在知识点或题目，请先删除关联内容");
        }
        courseMapper.deleteById(id);
    }

    private void apply(Course c, CourseDTO dto) {
        c.setName(dto.getName());
        c.setCode(dto.getCode());
        c.setSubject(dto.getSubject());
        c.setDescription(dto.getDescription());
        c.setCover(dto.getCover());
        c.setSort(dto.getSort() == null ? 0 : dto.getSort());
        c.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
    }
}
