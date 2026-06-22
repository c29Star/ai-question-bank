package com.aiqb.vo;

import com.aiqb.entity.Answer;
import com.aiqb.entity.Question;
import lombok.Data;

import java.util.List;

@Data
public class ExamResultVO {
    private Integer score;
    private Integer totalScore;
    private List<QuestionResult> results;

    @Data
    public static class QuestionResult {
        private Question question;
        private String userAnswer;
        private Boolean isCorrect;
        private Integer score;
    }
}
