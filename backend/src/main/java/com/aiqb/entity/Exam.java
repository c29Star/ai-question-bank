package com.aiqb.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exams")
public class Exam {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long paperId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Integer maxAttempts;
    /** DRAFT/PUBLISHED/ONGOING/FINISHED/ARCHIVED */
    private String status;
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @TableLogic
    private Integer deleted;
}
