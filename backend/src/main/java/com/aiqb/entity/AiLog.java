package com.aiqb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_logs")
public class AiLog {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** EXPLAIN/RECOMMEND/CLASSIFY */
    private String feature;
    private String input;
    private String output;
    private Integer tokensUsed;
    private Integer costMs;
    /** SUCCESS/FAILED */
    private String status;
    private String errorMsg;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
