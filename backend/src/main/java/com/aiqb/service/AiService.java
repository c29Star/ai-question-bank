package com.aiqb.service;

import com.aiqb.entity.Question;

import java.util.List;

public interface AiService {

    /** 生成题目解析 */
    String explainQuestion(Long questionId, Long userId);

    /** 推同类题（基于错题） */
    List<Question> recommendSimilar(Long wrongQuestionId, Integer count, Long userId);
}
