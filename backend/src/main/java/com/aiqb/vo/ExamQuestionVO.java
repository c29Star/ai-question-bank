package com.aiqb.vo;

import com.aiqb.entity.Question;
import lombok.Data;

@Data
public class ExamQuestionVO {
    private Long questionId;
    private Integer score;
    private Integer sortOrder;
    private Question question;
}
