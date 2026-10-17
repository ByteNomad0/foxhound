package com.kaoyan408.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kaoyan408.common.BizException;
import com.kaoyan408.common.PageResult;
import com.kaoyan408.entity.AiChatRecord;
import com.kaoyan408.entity.AnswerRecord;
import com.kaoyan408.entity.Course;
import com.kaoyan408.entity.KnowledgePoint;
import com.kaoyan408.entity.Question;
import com.kaoyan408.entity.User;
import com.kaoyan408.mapper.AiChatRecordMapper;
import com.kaoyan408.mapper.AnswerRecordMapper;
import com.kaoyan408.mapper.CodingProblemMapper;
import com.kaoyan408.mapper.CodingSubmissionMapper;
import com.kaoyan408.mapper.CourseMapper;
import com.kaoyan408.mapper.KnowledgePointMapper;
import com.kaoyan408.mapper.QuestionMapper;
import com.kaoyan408.mapper.UserMapper;
import com.kaoyan408.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final QuestionMapper questionMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final AiChatRecordMapper aiChatRecordMapper;
    private final CodingProblemMapper codingProblemMapper;
    private final CodingSubmissionMapper codingSubmissionMapper;

    public PageResult<User> listUsers(int page, int size, String keyword) {
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(User::getUsername, keyword).or().like(User::getNickname, keyword));
        }
        w.orderByAsc(User::getId);
        Page<User> p = userMapper.selectPage(new Page<>(page, size), w);
        return PageResult.of(p);
    }

    public void updateStatus(Long id, Integer status) {
        User u = requireUser(id);
        u.setStatus(status);
        userMapper.updateById(u);
    }

    public void updateRole(Long id, String role) {
        User u = requireUser(id);
        if ("admin".equals(u.getUsername())) {
            throw new BizException(400, "不能修改内置管理员的角色");
        }
        if (!"USER".equals(role) && !"ADMIN".equals(role)) {
            throw new BizException(400, "角色不合法");
        }
        u.setRole(role);
        userMapper.updateById(u);
    }

    public void deleteUser(Long id) {
        User u = requireUser(id);
        if ("admin".equals(u.getUsername())) {
            throw new BizException(400, "不能删除内置管理员账号");
        }
        userMapper.deleteById(id);
    }

    public DashboardVO dashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setUserCount(userMapper.selectCount(null));
        vo.setCourseCount(courseMapper.selectCount(null));
        vo.setKnowledgePointCount(knowledgePointMapper.selectCount(null));
        vo.setQuestionCount(questionMapper.selectCount(null));
        vo.setAnswerCount(answerRecordMapper.selectCount(null));
        vo.setAiChatCount(aiChatRecordMapper.selectCount(null));
        vo.setCodingProblemCount(codingProblemMapper.selectCount(null));
        vo.setCodingSubmissionCount(codingSubmissionMapper.selectCount(null));
        return vo;
    }

    private User requireUser(Long id) {
        User u = userMapper.selectById(id);
        if (u == null) {
            throw new BizException(404, "用户不存在");
        }
        return u;
    }
}
