package com.kaoyan408.vo;

import com.kaoyan408.dto.QuestionOption;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AnswerRecordVO {

    private Long id;

    private Long questionId;

    private String questionContent;

    private String questionType;

    private String courseName;

    private String knowledgePointTitle;

    private List<QuestionOption> options;

    private List<String> userAnswer;

    private List<String> correctAnswer;

    private Integer isCorrect;

    private String analysis;

    private LocalDateTime submittedAt;
}
