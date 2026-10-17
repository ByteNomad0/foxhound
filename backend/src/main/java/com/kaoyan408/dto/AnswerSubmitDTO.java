package com.kaoyan408.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AnswerSubmitDTO {

    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    /** 用户答案，如 ["A"]、["A","C"]、["T"] */
    private List<String> answer;
}
