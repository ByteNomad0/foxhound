package com.kaoyan408.vo;

import lombok.Data;

import java.util.List;

@Data
public class StatsVO {

    private Long totalAnswered;

    private Long correctCount;

    /** 总正确率百分比（0-100） */
    private Integer accuracy;

    private Long totalQuestions;

    private Long totalKnowledgePoints;

    private Long studiedKnowledgePoints;

    /** 整体学习进度百分比 */
    private Integer overallProgress;

    private List<CourseProgressVO> courseProgress;
}
