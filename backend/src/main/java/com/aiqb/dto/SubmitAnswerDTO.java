package com.aiqb.dto;

import lombok.Data;

import java.util.List;

@Data
public class SubmitAnswerDTO {

    private Long recordId;
    private Integer durationUsed;

    /** [{questionId, userAnswer, timeSpent}] */
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        private Long questionId;
        private String userAnswer;
        private Integer timeSpent;
    }
}
