package com.aiqb.dto;

import lombok.Data;

import java.util.List;

@Data
public class PaperDTO {
    private Long id;
    private String title;
    private String description;
    private Long subjectId;
    private Integer totalScore;
    private Integer duration;

    /** 题目 ID 列表（按顺序） */
    private List<Long> questionIds;

    /** 每题分值（与 questionIds 一一对应） */
    private List<Integer> scores;
}
