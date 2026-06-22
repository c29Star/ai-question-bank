package com.aiqb.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamDTO {

    private Long id;

    @NotNull(message = "试卷不能为空")
    private Long paperId;

    @NotBlank(message = "标题不能为空")
    private String title;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @NotNull(message = "时长不能为空")
    private Integer duration;

    @NotNull(message = "次数不能为空")
    private Integer maxAttempts;
}
