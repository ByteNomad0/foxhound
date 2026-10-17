package com.kaoyan408.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseDTO {

    @NotBlank(message = "课程名称不能为空")
    private String name;

    private String code;

    private String subject;

    private String description;

    private String cover;

    private Integer sort;

    private Integer status;
}
