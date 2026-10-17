package com.kaoyan408.vo;

import com.kaoyan408.dto.QuestionOption;
import lombok.Data;

import java.util.List;

/**
 * 题目视图对象。
 * 练习模式下 answer / analysis 为 null；复习、后台详情时返回完整内容。
 */
@Data
public class QuestionVO {

    private Long id;

    private Long courseId;

    private String courseName;

    private Long knowledgePointId;

    private String knowledgePointTitle;

    private String type;

    private String content;

    private List<QuestionOption> options;

    private List<String> answer;

    private String analysis;

    private Integer difficulty;
}
