package com.aiqb.service;

import com.aiqb.entity.WrongQuestion;
import com.aiqb.vo.WrongQuestionVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

public interface WrongQuestionService {

    /** 学生：我的错题本（不分页，兼容旧逻辑） */
    List<Map<String, Object>> myWrongQuestions(Long userId, Boolean mastered);

    /** 学生：我的错题（分页 + 关键字 + 掌握筛选） */
    IPage<WrongQuestionVO> page(Long userId, Boolean mastered, String keyword, Integer current, Integer size);

    /** 标记掌握 */
    void markMastered(Long id, Long userId, Boolean mastered);

    /** 错题按知识点统计 */
    List<Map<String, Object>> statsByKnowledge(Long userId);
}
