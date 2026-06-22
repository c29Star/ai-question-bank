package com.aiqb.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionDTO {

    private Long id;

    @NotNull(message = "科目不能为空")
    private Long subjectId;

    @NotBlank(message = "题型不能为空")
    /** SINGLE / MULTIPLE / JUDGE / ESSAY */
    private String type;

    @NotNull(message = "难度不能为空")
    private Integer difficulty;

    @NotBlank(message = "题干不能为空")
    private String content;

    /** JSON 数组字符串 */
    private String options;

    @NotBlank(message = "答案不能为空")
    private String answer;

    private String explanation;

    private String knowledgePoint;
}
