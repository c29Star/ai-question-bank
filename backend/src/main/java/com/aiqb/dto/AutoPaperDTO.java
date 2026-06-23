package com.aiqb.dto;

import lombok.Data;

import java.util.List;

@Data
public class AutoPaperDTO {
    private String title;
    private String description;
    private Long subjectId;
    private Integer duration;

    /**
     * 配置项：[{ type: "SINGLE", count: 10, score: 2, difficulty: null }]
     */
    private List<PaperRule> rules;

    @Data
    public static class PaperRule {
        /** SINGLE / MULTIPLE / JUDGE / ESSAY */
        private String type;
        private Integer count;
        private Integer score = 5;     // 默认每题 5 分（前端不传也能跑）
        private Integer difficulty;
        private String knowledgePoint;
    }
}
