package com.kaoyan408.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kaoyan408.entity.StudyRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface StudyRecordMapper extends BaseMapper<StudyRecord> {

    /** 用户已学习（去重）的知识点数量 */
    @Select("SELECT COUNT(DISTINCT knowledge_point_id) FROM study_record WHERE user_id = #{userId}")
    Long countStudiedKp(@Param("userId") Long userId);
}
