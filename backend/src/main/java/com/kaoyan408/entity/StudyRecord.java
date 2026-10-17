package com.kaoyan408.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("study_record")
public class StudyRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long knowledgePointId;

    private Integer studied;

    private LocalDateTime lastStudiedAt;
}
