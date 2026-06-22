package com.aiqb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("answers")
public class Answer {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private Long questionId;
    private String userAnswer;
    private Boolean isCorrect;
    private Integer score;
    private Integer timeSpent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
