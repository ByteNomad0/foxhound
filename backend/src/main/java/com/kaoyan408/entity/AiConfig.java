package com.kaoyan408.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_config")
public class AiConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String provider;

    private String baseUrl;

    private String apiKey;

    private String model;

    private String systemPrompt;

    private Integer enabled;

    private LocalDateTime updatedAt;
}
