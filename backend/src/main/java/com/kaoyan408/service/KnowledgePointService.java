package com.kaoyan408.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kaoyan408.common.BizException;
import com.kaoyan408.dto.KnowledgePointDTO;
import com.kaoyan408.entity.KnowledgePoint;
import com.kaoyan408.entity.Question;
import com.kaoyan408.mapper.KnowledgePointMapper;
import com.kaoyan408.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgePointService {

    private final KnowledgePointMapper knowledgePointMapper;
    private final QuestionMapper questionMapper;

    public List<KnowledgePoint> listByCourse(Long courseId) {
        return knowledgePointMapper.selectList(new LambdaQueryWrapper<KnowledgePoint>()
                .eq(KnowledgePoint::getCourseId, courseId)
                .orderByAsc(KnowledgePoint::getSort)
                .orderByAsc(KnowledgePoint::getId));
    }

    public KnowledgePoint getDetail(Long id) {
        KnowledgePoint kp = knowledgePointMapper.selectById(id);
        if (kp == null) {
            throw new BizException(404, "知识点不存在");
        }
        return kp;
    }

    public KnowledgePoint create(KnowledgePointDTO dto) {
        KnowledgePoint kp = new KnowledgePoint();
        apply(kp, dto);
        knowledgePointMapper.insert(kp);
        return kp;
    }

    public KnowledgePoint update(Long id, KnowledgePointDTO dto) {
        KnowledgePoint kp = getDetail(id);
        apply(kp, dto);
        knowledgePointMapper.updateById(kp);
        return kp;
    }

    public void delete(Long id) {
        getDetail(id);
        Long childCount = knowledgePointMapper.selectCount(
                new LambdaQueryWrapper<KnowledgePoint>().eq(KnowledgePoint::getParentId, id));
        Long qCount = questionMapper.selectCount(
                new LambdaQueryWrapper<Question>().eq(Question::getKnowledgePointId, id));
        if ((childCount != null && childCount > 0) || (qCount != null && qCount > 0)) {
            throw new BizException(400, "该知识点下存在子知识点或题目，请先删除关联内容");
        }
        knowledgePointMapper.deleteById(id);
    }

    private void apply(KnowledgePoint kp, KnowledgePointDTO dto) {
        kp.setCourseId(dto.getCourseId());
        kp.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        kp.setTitle(dto.getTitle());
        kp.setContent(dto.getContent());
        kp.setKeyPoint(dto.getKeyPoint());
        kp.setDifficultPoint(dto.getDifficultPoint());
        kp.setSort(dto.getSort() == null ? 0 : dto.getSort());
    }
}
