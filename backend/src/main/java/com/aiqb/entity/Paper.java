package com.aiqb.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("papers")
public class Paper {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private Long subjectId;
    private Integer totalScore;
    private Integer duration;
    private Long createdBy;

    /** 非持久化字段：详情接口动态注入学科名，给前端预览弹窗用 */
    @TableField(exist = false)
    private String subjectName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @JsonIgnore
    @TableLogic
    private Integer deleted;
}
