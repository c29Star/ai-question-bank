package com.aiqb.vo;

import com.aiqb.entity.Exam;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamVO {
    private Long id;
    private Long paperId;
    private String paperName;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Integer maxAttempts;
    private String status;
    private Long createdBy;
    private String createdAt;

    public static ExamVO from(Exam e, String paperName) {
        ExamVO v = new ExamVO();
        v.id = e.getId();
        v.paperId = e.getPaperId();
        v.paperName = paperName;
        v.title = e.getTitle();
        v.startTime = e.getStartTime();
        v.endTime = e.getEndTime();
        v.duration = e.getDuration();
        v.maxAttempts = e.getMaxAttempts();
        v.status = e.getStatus();
        v.createdBy = e.getCreatedBy();
        v.createdAt = e.getCreatedAt() == null ? null : e.getCreatedAt().toString();
        return v;
    }
}
