package com.kaoyan408.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KnowledgePointDTO {

    @NotNull(message = "课程不能为空")
    private Long courseId;

    private Long parentId;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;

    private String keyPoint;

    private String difficultPoint;

    private Integer sort;
}
