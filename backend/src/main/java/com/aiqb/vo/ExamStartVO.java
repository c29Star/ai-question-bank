package com.aiqb.vo;

import com.aiqb.entity.ExamRecord;
import com.aiqb.entity.Paper;
import com.aiqb.entity.Question;
import lombok.Data;

import java.util.List;

@Data
public class ExamStartVO {
    private ExamRecord record;
    private Paper paper;
    /** 题目列表（包含分值） */
    private List<ExamQuestionVO> questions;
}
