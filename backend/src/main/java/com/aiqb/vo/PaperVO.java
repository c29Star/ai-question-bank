package com.aiqb.vo;

import com.aiqb.entity.Paper;
import lombok.Data;

import java.util.List;

@Data
public class PaperVO {
    private Long id;
    private String title;
    private String description;
    private Long subjectId;
    private String subjectName;
    private Integer totalScore;
    private Integer duration;
    private Integer questionCount;
    private Long createdBy;
    private String createdAt;

    public static PaperVO from(Paper p, String subjectName, Integer questionCount) {
        PaperVO v = new PaperVO();
        v.id = p.getId();
        v.title = p.getTitle();
        v.description = p.getDescription();
        v.subjectId = p.getSubjectId();
        v.subjectName = subjectName;
        v.totalScore = p.getTotalScore();
        v.duration = p.getDuration();
        v.questionCount = questionCount;
        v.createdBy = p.getCreatedBy();
        v.createdAt = p.getCreatedAt() == null ? null : p.getCreatedAt().toString();
        return v;
    }
}
