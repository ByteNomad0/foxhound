package com.kaoyan408.vo;

import lombok.Data;

@Data
public class CourseProgressVO {

    private Long courseId;

    private String courseName;

    private Long totalKp;

    private Long studiedKp;

    /** 学习进度百分比（0-100，四舍五入） */
    private Integer progress;

    private Long answeredCount;

    private Long correctCount;

    /** 正确率百分比 */
    private Integer accuracy;
}
