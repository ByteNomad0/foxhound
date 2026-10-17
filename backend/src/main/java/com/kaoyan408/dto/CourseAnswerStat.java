package com.kaoyan408.dto;

import lombok.Data;

/**
 * 按课程统计的答题数据（用于学习统计）
 */
@Data
public class CourseAnswerStat {

    private Long courseId;

    private Long total;

    private Long correct;
}
