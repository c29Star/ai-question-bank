package com.aiqb.service;

import com.aiqb.entity.WrongQuestion;

import java.util.List;
import java.util.Map;

public interface WrongQuestionService {

    /** 学生：我的错题本 */
    List<Map<String, Object>> myWrongQuestions(Long userId, Boolean mastered);

    /** 标记掌握 */
    void markMastered(Long id, Long userId, Boolean mastered);

    /** 错题按知识点统计 */
    List<Map<String, Object>> statsByKnowledge(Long userId);
}
