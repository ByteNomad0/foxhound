package com.kaoyan408.vo;

import lombok.Data;

@Data
public class WeakPointVO {

    private Long knowledgePointId;

    private String knowledgePointTitle;

    private String courseName;

    private Long total;

    private Long correct;

    /** 正确率百分比 */
    private Integer accuracy;
}
