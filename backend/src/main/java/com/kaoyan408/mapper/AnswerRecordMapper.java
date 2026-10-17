package com.kaoyan408.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kaoyan408.dto.CourseAnswerStat;
import com.kaoyan408.entity.AnswerRecord;
import com.kaoyan408.vo.WeakPointVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AnswerRecordMapper extends BaseMapper<AnswerRecord> {

    /** 按知识点统计答题情况，用于薄弱点分析 */
    @Select("SELECT q.knowledge_point_id AS knowledgePointId, kp.title AS knowledgePointTitle, " +
            "c.name AS courseName, COUNT(*) AS total, CAST(SUM(ar.is_correct) AS SIGNED) AS correct " +
            "FROM answer_record ar " +
            "JOIN question q ON ar.question_id = q.id " +
            "LEFT JOIN knowledge_point kp ON q.knowledge_point_id = kp.id " +
            "LEFT JOIN course c ON q.course_id = c.id " +
            "WHERE ar.user_id = #{userId} AND q.knowledge_point_id IS NOT NULL " +
            "GROUP BY q.knowledge_point_id, kp.title, c.name")
    List<WeakPointVO> selectWeakPoints(@Param("userId") Long userId);

    /** 按课程统计答题总数与正确数 */
    @Select("SELECT q.course_id AS courseId, COUNT(*) AS total, CAST(SUM(ar.is_correct) AS SIGNED) AS correct " +
            "FROM answer_record ar " +
            "JOIN question q ON ar.question_id = q.id " +
            "WHERE ar.user_id = #{userId} " +
            "GROUP BY q.course_id")
    List<CourseAnswerStat> selectStatsByCourse(@Param("userId") Long userId);
}
