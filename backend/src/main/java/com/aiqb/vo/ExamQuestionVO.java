package com.aiqb.vo;

import lombok.Data;

@Data
public class ExamQuestionVO {
    private Long questionId;
    private Integer score;
    private Integer sortOrder;
    /** 题目详情（options 已解析为 List<String>；studentAnswer / correctAnswer 已分离开） */
    private QuestionVO question;
    /** 学生此前已保存的答案（仅在续答/查看记录时有值，初始进入考试时通常为 null） */
    private String savedAnswer;
}
