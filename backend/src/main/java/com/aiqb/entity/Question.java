package com.aiqb.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("questions")
public class Question {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long subjectId;
    /** SINGLE/MULTIPLE/JUDGE/ESSAY */
    private String type;
    private Integer difficulty;
    private String content;
    /** JSON 数组字符串 */
    private String options;
    private String answer;
    private String explanation;
    private String knowledgePoint;
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @TableLogic
    private Integer deleted;
}
