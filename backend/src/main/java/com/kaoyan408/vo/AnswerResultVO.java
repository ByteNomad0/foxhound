package com.kaoyan408.vo;

import lombok.Data;

import java.util.List;

@Data
public class AnswerResultVO {

    private Long questionId;

    private String type;

    private Boolean correct;

    private List<String> userAnswer;

    private List<String> correctAnswer;

    private String analysis;
}
