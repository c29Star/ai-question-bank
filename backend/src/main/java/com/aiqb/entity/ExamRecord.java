package com.aiqb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("exam_records")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examId;
    private Long userId;
    private Long paperId;
    private Integer score;
    private Integer totalScore;
    /** IN_PROGRESS/SUBMITTED/GRADED */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime startTime;

    private LocalDateTime submitTime;
    private Integer durationUsed;
}
