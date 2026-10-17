package com.kaoyan408.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("question")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long courseId;

    private Long knowledgePointId;

    /** SINGLE / MULTI / JUDGE */
    private String type;

    private String content;

    /** JSON 数组：[{"key":"A","content":"..."}] */
    private String options;

    /** JSON 数组：["A"] / ["A","C"] / ["T"] / ["F"] */
    private String answer;

    private String analysis;

    private Integer difficulty;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
