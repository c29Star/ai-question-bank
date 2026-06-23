package com.aiqb.vo;

import lombok.Data;

import java.util.List;

@Data
public class WrongQuestionVO {
    private Long id;
    private Long userId;
    private Long questionId;
    private String type;
    private Integer difficulty;
    private String content;
    private List<String> options;
    private String correctAnswer;
    private String explanation;
    private String knowledgePoint;
    private Long subjectId;
    private String subjectName;
    private Integer wrongCount;
    private Boolean mastered;
    private String lastWrongAt;
}
