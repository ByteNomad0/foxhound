package com.kaoyan408.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiChatDTO {

    @NotBlank(message = "问题不能为空")
    private String message;

    /** 可选，指定课程，便于带入上下文 */
    private Long courseId;

    /** 可选，指定知识点 */
    private Long knowledgePointId;
}
