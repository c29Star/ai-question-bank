package com.aiqb.vo;

import lombok.Data;

import java.util.List;

@Data
public class ExamResultVO {
    private Integer score;
    private Integer totalScore;
    private List<QuestionResult> results;

    @Data
    public static class QuestionResult {
        /** 题目详情（含正确答案 + 解析 + options 数组）—— 交卷后学生应能看到对错与解析 */
        private QuestionVO question;
        private String userAnswer;
        private Boolean isCorrect;
        private Integer score;
    }
}
