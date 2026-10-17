package com.kaoyan408.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class QuestionDTO {

    @NotNull(message = "课程不能为空")
    private Long courseId;

    private Long knowledgePointId;

    @NotBlank(message = "题型不能为空")
    private String type;

    @NotBlank(message = "题干不能为空")
    private String content;

    private List<QuestionOption> options;

    /** 正确答案，如 ["A"]、["A","C"]、["T"] */
    @NotNull(message = "正确答案不能为空")
    private List<String> answer;

    private String analysis;

    private Integer difficulty;
}
