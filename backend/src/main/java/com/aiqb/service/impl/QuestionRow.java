package com.aiqb.service.impl;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Excel 题目行
 */
@Data
public class QuestionRow {

    @ExcelProperty("题型(SINGLE/MULTIPLE/JUDGE/ESSAY)")
    private String type;

    @ExcelProperty("难度(1-5)")
    private Integer difficulty;

    @ExcelProperty("题干")
    private String content;

    @ExcelProperty("选项(JSON数组)")
    private String options;

    @ExcelProperty("答案")
    private String answer;

    @ExcelProperty("解析")
    private String explanation;

    @ExcelProperty("知识点")
    private String knowledgePoint;
}
