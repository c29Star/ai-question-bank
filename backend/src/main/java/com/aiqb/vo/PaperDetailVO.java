package com.aiqb.vo;

import com.aiqb.entity.Paper;
import lombok.Data;

import java.util.List;

@Data
public class PaperDetailVO {
    private Paper paper;
    /** 题目详情（options 已解析为 List<String>；按调用方角色决定是否含 answer/explanation） */
    private List<QuestionVO> questions;
}
