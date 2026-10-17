package com.kaoyan408.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kaoyan408.dto.CourseAnswerStat;
import com.kaoyan408.dto.DailyAnswerCount;
import com.kaoyan408.entity.AnswerRecord;
import com.kaoyan408.entity.Course;
import com.kaoyan408.entity.KnowledgePoint;
import com.kaoyan408.entity.Question;
import com.kaoyan408.entity.StudyRecord;
import com.kaoyan408.mapper.AnswerRecordMapper;
import com.kaoyan408.mapper.CourseMapper;
import com.kaoyan408.mapper.KnowledgePointMapper;
import com.kaoyan408.mapper.QuestionMapper;
import com.kaoyan408.mapper.StudyRecordMapper;
import com.kaoyan408.vo.CourseProgressVO;
import com.kaoyan408.vo.StatsVO;
import com.kaoyan408.vo.WeakPointVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyService {

    private final StudyRecordMapper studyRecordMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final CourseMapper courseMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final QuestionMapper questionMapper;

    /** 标记知识点为已学习 */
    public void markStudied(Long userId, Long knowledgePointId) {
        StudyRecord existing = studyRecordMapper.selectOne(new LambdaQueryWrapper<StudyRecord>()
                .eq(StudyRecord::getUserId, userId)
                .eq(StudyRecord::getKnowledgePointId, knowledgePointId));
        if (existing == null) {
            StudyRecord r = new StudyRecord();
            r.setUserId(userId);
            r.setKnowledgePointId(knowledgePointId);
            r.setStudied(1);
            r.setLastStudiedAt(LocalDateTime.now());
            studyRecordMapper.insert(r);
        } else {
            existing.setStudied(1);
            existing.setLastStudiedAt(LocalDateTime.now());
            studyRecordMapper.updateById(existing);
        }
    }

    /** 用户已学习（去重）的知识点 ID 列表 */
    public List<Long> getStudiedIds(Long userId) {
        return studyRecordMapper.selectList(new LambdaQueryWrapper<StudyRecord>()
                        .eq(StudyRecord::getUserId, userId))
                .stream().map(StudyRecord::getKnowledgePointId).distinct().toList();
    }

    /** 学习日历：按月统计每天的答题数量 */
    public List<DailyAnswerCount> calendar(Long userId, int year, int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime end = start.plusMonths(1);
        return answerRecordMapper.selectDailyCounts(userId, start, end);
    }

    /** 学习统计：正确率 + 学习进度 */
    public StatsVO stats(Long userId) {
        StatsVO vo = new StatsVO();

        long totalAnswered = answerRecordMapper.selectCount(
                new LambdaQueryWrapper<AnswerRecord>().eq(AnswerRecord::getUserId, userId));
        long correctCount = answerRecordMapper.selectCount(
                new LambdaQueryWrapper<AnswerRecord>()
                        .eq(AnswerRecord::getUserId, userId)
                        .eq(AnswerRecord::getIsCorrect, 1));
        vo.setTotalAnswered(totalAnswered);
        vo.setCorrectCount(correctCount);
        vo.setAccuracy(percent(correctCount, totalAnswered));

        long totalQuestions = questionMapper.selectCount(
                new LambdaQueryWrapper<Question>().eq(Question::getStatus, 1));
        vo.setTotalQuestions(totalQuestions);

        long totalKp = knowledgePointMapper.selectCount(null);
        long studiedKp = studyRecordMapper.countStudiedKp(userId);
        vo.setTotalKnowledgePoints(totalKp);
        vo.setStudiedKnowledgePoints(studiedKp);
        vo.setOverallProgress(percent(studiedKp, totalKp));

        // 各课程进度
        List<Course> courses = courseMapper.selectList(
                new LambdaQueryWrapper<Course>().eq(Course::getStatus, 1).orderByAsc(Course::getSort));
        List<KnowledgePoint> kps = knowledgePointMapper.selectList(null);
        Map<Long, Long> kpToCourse = kps.stream().collect(Collectors.toMap(
                KnowledgePoint::getId, KnowledgePoint::getCourseId, (a, b) -> a));
        Set<Long> studiedIds = studyRecordMapper.selectList(
                        new LambdaQueryWrapper<StudyRecord>().eq(StudyRecord::getUserId, userId))
                .stream().map(StudyRecord::getKnowledgePointId).collect(Collectors.toSet());
        Map<Long, CourseAnswerStat> answerStats = answerRecordMapper.selectStatsByCourse(userId).stream()
                .collect(Collectors.toMap(CourseAnswerStat::getCourseId, Function.identity(), (a, b) -> a));

        List<CourseProgressVO> cpList = new ArrayList<>();
        for (Course c : courses) {
            CourseProgressVO cp = new CourseProgressVO();
            cp.setCourseId(c.getId());
            cp.setCourseName(c.getName());
            long courseTotalKp = kps.stream().filter(k -> c.getId().equals(k.getCourseId())).count();
            long courseStudied = kps.stream()
                    .filter(k -> c.getId().equals(k.getCourseId()) && studiedIds.contains(k.getId()))
                    .count();
            cp.setTotalKp(courseTotalKp);
            cp.setStudiedKp(courseStudied);
            cp.setProgress(percent(courseStudied, courseTotalKp));

            CourseAnswerStat st = answerStats.get(c.getId());
            long answered = st == null ? 0 : st.getTotal();
            long correct = st == null ? 0 : st.getCorrect();
            cp.setAnsweredCount(answered);
            cp.setCorrectCount(correct);
            cp.setAccuracy(percent(correct, answered));
            cpList.add(cp);
        }
        vo.setCourseProgress(cpList);
        return vo;
    }

    /** 各知识点正确率（升序，用于薄弱点分析） */
    public List<WeakPointVO> weakPoints(Long userId) {
        List<WeakPointVO> list = answerRecordMapper.selectWeakPoints(userId);
        for (WeakPointVO w : list) {
            long total = w.getTotal() == null ? 0 : w.getTotal();
            long correct = w.getCorrect() == null ? 0 : w.getCorrect();
            w.setAccuracy(percent(correct, total));
        }
        list.sort(Comparator.comparing(WeakPointVO::getAccuracy, Comparator.nullsLast(Integer::compareTo)));
        return list;
    }

    private int percent(long num, long den) {
        return den == 0 ? 0 : (int) Math.round(num * 100.0 / den);
    }
}
