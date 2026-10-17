package com.kaoyan408.dto;

import lombok.Data;

@Data
public class AiConfigDTO {

    private String provider;

    private String baseUrl;

    /** 若为空或已脱敏（以****开头）则保持原密钥不变 */
    private String apiKey;

    private String model;

    private String systemPrompt;

    private Integer enabled;
}
