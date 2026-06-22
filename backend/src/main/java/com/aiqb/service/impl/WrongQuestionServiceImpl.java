package com.aiqb.service.impl;

import com.aiqb.common.BusinessException;
import com.aiqb.entity.WrongQuestion;
import com.aiqb.mapper.QuestionMapper;
import com.aiqb.mapper.WrongQuestionMapper;
import com.aiqb.service.WrongQuestionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WrongQuestionServiceImpl implements WrongQuestionService {

    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionMapper questionMapper;

    @Override
    public List<Map<String, Object>> myWrongQuestions(Long userId, Boolean mastered) {
        List<Map<String, Object>> all = wrongQuestionMapper.selectWithQuestion(userId);
        if (mastered == null) return all;
        return all.stream()
                .filter(m -> mastered.equals(m.get("mastered")))
                .collect(Collectors.toList());
    }

    @Override
    public void markMastered(Long id, Long userId, Boolean mastered) {
        WrongQuestion wq = wrongQuestionMapper.selectById(id);
        if (wq == null) throw BusinessException.notFound("错题记录不存在");
        if (!wq.getUserId().equals(userId)) throw BusinessException.forbidden("无权操作");
        wq.setMastered(mastered);
        wrongQuestionMapper.updateById(wq);
    }

    @Override
    public List<Map<String, Object>> statsByKnowledge(Long userId) {
        return questionMapper.countByKnowledge(userId);
    }
}
